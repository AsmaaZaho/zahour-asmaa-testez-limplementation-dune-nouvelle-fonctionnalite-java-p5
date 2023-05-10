package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void testProcessIncomingVehicle(){

        when(inputReaderUtil.readSelection()).thenReturn(1); //voiture
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true); //sauvegarde ok
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1); //place numéro 1
        when(ticketDAO.getNbTicket(any(Ticket.class))).thenReturn(0); //jamais venu

        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO,Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, times(1)).getNbTicket(any(Ticket.class));
    }

    @Test
    public void processExitingVehicleTest() throws Exception{
        
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        
        parkingService.processExitingVehicle();
        
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getTicket("ABCDEF");
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception{

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        parkingService.processExitingVehicle();

        //verifier que le parking pas updaté et que la place est pas dispo
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        Assertions.assertFalse(parkingSpot.isAvailable());
        Assertions.assertEquals(0, parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class)));
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
    
    }

    @Test
    public void testGetNextParkingNumberIfAvailable(){

        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(1);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        Assertions.assertEquals(1, parkingSpot.getId());
        Assertions.assertTrue(parkingSpot.isAvailable());

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() throws Exception{
     
        when(inputReaderUtil.readSelection()).thenReturn(2);
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(0);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        verify(inputReaderUtil).readSelection();
        verify(parkingSpotDAO,Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        Assertions.assertNull(parkingSpot);
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() throws Exception{
        
        when(inputReaderUtil.readSelection()).thenReturn(3);
        
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        verify(parkingSpotDAO, Mockito.times(0)).getNextAvailableSlot(any(ParkingType.class));
        Assertions.assertNull(parkingSpot);
    }
    
}