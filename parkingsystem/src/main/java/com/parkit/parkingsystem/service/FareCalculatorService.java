package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
        //we get the entry time and the exit time in milliseconds using the method getTime().
        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //this conversion to hours helps to resolve the bug to calculate the price of a ticket.
        double duration = (outHour - inHour)/(3600.0 * 1000.0);

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                //if parking time is less than 30 minutes(0.5 heures) , parking fare will be 0.
                ticket.setPrice(duration > 0.5 ? duration * Fare.CAR_RATE_PER_HOUR : 0);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration > 0.5 ? duration * Fare.BIKE_RATE_PER_HOUR : 0);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
        if(discount){
            ticket.setPrice(ticket.getPrice() * 0.95);//the user can benefit from a reduction of 5% if he regularly uses the parking
        }
    }

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);

    }
}