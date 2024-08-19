package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

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
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        
        assertNotNull(ticketDAO.getTicket("ABCDEF"));
        
        assertFalse(ticketDAO.getTicket("ABCDEF").getParkingSpot().isAvailable());
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        Ticket ticket = new Ticket();
        ticket = ticketDAO.getTicket("ABCDEF");
        ticket.setOutTime(new Date(System.currentTimeMillis() + (  30 * 60 * 1000)));
        ticketDAO.updateTicket(ticket);
        
        parkingService.processExitingVehicle();
        
        assertEquals( Math.ceil((0.5 * Fare.CAR_RATE_PER_HOUR) * 100) /100 , ticketDAO.getTicket("ABCDEF").getPrice());
        assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime());
    }
    
    @Test
    public void testParkingLotExitRecurringUser() {
    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(0);
        ticket.setInTime(new Date(System.currentTimeMillis() - (  60 * 60 * 1000)));
        ticket.setOutTime(new Date(System.currentTimeMillis() - (  30 * 60 * 1000)));
        ticketDAO.saveTicket(ticket);
        
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        
        Ticket ticketExit = new Ticket();
        ticketExit = ticketDAO.getTicket("ABCDEF");
        ticketExit.setOutTime(new Date(System.currentTimeMillis() + (  30 * 60 * 1000)));
        ticketDAO.updateTicket(ticketExit);
        
        parkingService.processExitingVehicle();
        
        assertEquals(2, ticketDAO.getNbTicket("ABCDEF"));
        assertEquals(Math.ceil((0.5 * Fare.CAR_RATE_PER_HOUR * 0.95) * 100) /100, ticketDAO.getTicket("ABCDEF").getPrice());
	}
    
    /*@Test
    public void testParkingLotExitRecurringUser() {
    	testParkingLotExit();
    	
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();
        
        assertEquals(2, ticketDAO.getNbTicket("ABCDEF"));
        assertEquals(0, ticketDAO.getTicket("ABCDEF").getPrice());
	}*/
}
