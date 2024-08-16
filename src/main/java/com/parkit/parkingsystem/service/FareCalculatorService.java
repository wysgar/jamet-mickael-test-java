package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }
    
    public void calculateFare(Ticket ticket, boolean discount){
    	if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour =  ticket.getInTime().getTime();
        long outHour =  ticket.getOutTime().getTime();

        double duration = (double) (outHour - inHour) / 60000 / 60;
        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
            	if (duration < 0.5) {
            		ticket.setPrice(0);
            	} 
            	else if (discount == true) {
            		ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR * 0.95);
				}
            	else {
            		ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
				}
                break;
            }
            case BIKE: {
            	if (duration < 0.5) {
            		ticket.setPrice(0);
            	}
            	else if (discount == true) {
            		ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR * 0.95);
				}
            	else {
            		ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
				}
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}