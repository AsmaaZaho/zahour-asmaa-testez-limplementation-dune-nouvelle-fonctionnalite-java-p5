package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.model.ParkingSpot;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import java.util.concurrent.TimeUnit;

import java.util.Date;



@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    //private Ticket ticket;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("123456");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        parkingService.processIncomingVehicle();
        
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
        Ticket ticket = ticketDAO.getTicket("123456");
        assertNotNull(ticket);
        int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertNotEquals(1, nextAvailableSlot);
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
       
        try{
            TimeUnit.SECONDS.sleep(5);
        } catch(InterruptedException e) {
		    e.printStackTrace();
		}

        parkingService.processExitingVehicle();

        //TODO: check that the fare generated and out time are populated correctly in the database
        Ticket ticket = ticketDAO.getTicket("123456");
        parkingSpotDAO.updateParking(ticket.getParkingSpot());
        assertNotEquals(null, ticket.getOutTime());
        assertNotNull(ticket.getPrice()); //not null
    }

    
    @Test
    public void testParkingLotExitRecurringUser() throws Exception{
        
        testParkingLotExit();
        //Given
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (  120 * 60 * 1000)));
        ticket.setVehicleRegNumber("123456");
        ticket.setParkingSpot(parkingService.getNextParkingNumberIfAvailable());
        ticketDAO.saveTicket(ticket);

        // Wait for some time to simulate parking duration
        try {
		    TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    
        // Recurrent user exits parking lot
        parkingService.processExitingVehicle();


        // Verify that ticket is saved and we have 2 tickets
        Ticket savedTicket = ticketDAO.getTicket("123456");
        assertNotNull(savedTicket);
        assertEquals(ParkingType.CAR, savedTicket.getParkingSpot().getParkingType());
        assertEquals("123456", savedTicket.getVehicleRegNumber());
        assertTrue(savedTicket.getPrice() > 0);
        assertTrue(savedTicket.getPrice() < 3);
        assertEquals(2, ticketDAO.getNbTicket(savedTicket));
        

    }
    
}
