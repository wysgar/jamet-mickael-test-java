package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;
    
    private static Ticket ticket;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() throws Exception{
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
    	
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(any(String.class));
    }
    
    @Test
    public void testProcessIncomingVehicle() throws Exception{
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    	
        parkingService.processIncomingVehicle();
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(any(String.class));
    }
    
    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception{
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(any(String.class));
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailable(){
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
    	
    	parkingService.getNextParkingNumberIfAvailable();
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() throws Exception{
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
    	
    	parkingService.processIncomingVehicle();
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(inputReaderUtil, Mockito.times(0)).readVehicleRegistrationNumber();
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument (){
    	when(inputReaderUtil.readSelection()).thenReturn(3);
    	
    	parkingService.getNextParkingNumberIfAvailable();
    	verify(inputReaderUtil, Mockito.times(1)).readSelection();
        verify(parkingSpotDAO, Mockito.times(0)).getNextAvailableSlot(any(ParkingType.class));
    }

}
