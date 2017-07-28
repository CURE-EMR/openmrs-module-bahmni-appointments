package org.openmrs.module.appointments.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.web.contract.AppointmentServiceFullResponse;
import org.openmrs.module.appointments.web.contract.AppointmentServicePayload;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;


public class AppointmentServiceControllerTest {

    @Mock
    private AppointmentServiceService appointmentServiceService;

    @Mock
    private AppointmentServiceMapper appointmentServiceMapper;

    @InjectMocks
    private AppointmentServiceController appointmentServiceController;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldCreateAppointmentService() throws Exception {
        AppointmentServicePayload appointmentServicePayload = new AppointmentServicePayload();
        appointmentServicePayload.setName("Cardio");
        AppointmentService mappedServicePayload = new AppointmentService();
        when(appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload)).thenReturn(mappedServicePayload);
        when(appointmentServiceService.save(mappedServicePayload)).thenReturn(mappedServicePayload);
        AppointmentServiceFullResponse response = new AppointmentServiceFullResponse();
        when(appointmentServiceMapper.constructResponse(mappedServicePayload)).thenReturn(response);

        ResponseEntity<Object> savedAppointmentService = appointmentServiceController.createAppointmentService(appointmentServicePayload);

        verify(appointmentServiceMapper, times(1)).getAppointmentServiceFromPayload(appointmentServicePayload);
        verify(appointmentServiceService, times(1)).save(any(AppointmentService.class));
        verify(appointmentServiceMapper, times(1)).constructResponse(mappedServicePayload);
        assertNotNull(savedAppointmentService);
        assertEquals(response, savedAppointmentService.getBody());
        assertEquals("200", savedAppointmentService.getStatusCode().toString());
    }

    @Test
    public void shouldThrowExceptionWhenCreateAppointmentServiceWhenEmptyPayloadIsGiven() throws Exception {
        AppointmentServicePayload appointmentServicePayload = new AppointmentServicePayload();

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Appointment Service name should not be null");

        appointmentServiceController.createAppointmentService(appointmentServicePayload);
    }

    @Test
    public void shouldValidateTheAppointmentServiceAndThrowAnExceptionWhenThereIsNonVoidedAppointmentServiceWithTheSameName() throws Exception {
        AppointmentServicePayload appointmentServicePayload = new AppointmentServicePayload();
        appointmentServicePayload.setName("Cardio");
        AppointmentService mappedServicePayload = new AppointmentService();
        when(appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload)).thenReturn(mappedServicePayload);
        when(appointmentServiceService.save(mappedServicePayload)).thenThrow(new RuntimeException("The service 'Cardio' is already present"));

        ResponseEntity<Object> appointmentService = appointmentServiceController.createAppointmentService(appointmentServicePayload);

        assertNotNull(appointmentService);
        assertEquals("400", appointmentService.getStatusCode().toString());
        assertEquals("The service 'Cardio' is already present", ((RuntimeException)appointmentService.getBody()).getMessage());
    }

    @Test
    public void shouldGetAllAppointmentServices() throws Exception {
        AppointmentService appointmentService = new AppointmentService();
        List<AppointmentService> appointmentServiceList = new ArrayList<>();
        appointmentServiceList.add(appointmentService);
        when(appointmentServiceService.getAllAppointmentServices(false)).thenReturn(appointmentServiceList);
        
        appointmentServiceController.getAllAppointmentServices();
        verify(appointmentServiceService, times(1)).getAllAppointmentServices(false);
        verify(appointmentServiceMapper, times(1)).constructResponse(appointmentServiceList);
    }
    
    @Test
    public void shouldGetAppointmentServiceByUUID() throws Exception {
        String uuid = "c36006d4-9fbb-4f20-866b-0ece245615b1";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid(uuid);
        when(appointmentServiceService.getAppointmentServiceByUuid(uuid)).thenReturn(appointmentService);
        
        appointmentServiceController.getAppointmentServiceByUuid(uuid);
        verify(appointmentServiceService, times(1)).getAppointmentServiceByUuid(uuid);
        verify(appointmentServiceMapper, times(1)).constructResponse(appointmentService);
    }
    
    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionIfServiceNotFound() throws Exception {
        appointmentServiceController.getAppointmentServiceByUuid("random");
        verify(appointmentServiceService, times(1)).getAppointmentServiceByUuid("random");
    }

    @Test
    public void shouldVoidTheAppointmentService() throws Exception {
        String appointmentServiceUuid = "appointmentServiceUuid";
        String voidReason = "voidReason";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid(appointmentServiceUuid);
        appointmentService.setName("serviceName");
        when(appointmentServiceService.getAppointmentServiceByUuid(appointmentServiceUuid)).thenReturn(appointmentService);
        when(appointmentServiceService.voidAppointmentService(appointmentService, voidReason)).thenReturn(appointmentService);

        appointmentServiceController.voidAppointmentService(appointmentServiceUuid, voidReason);

        verify(appointmentServiceService, times(1)).getAppointmentServiceByUuid(appointmentServiceUuid);
        verify(appointmentServiceService, times(1)).voidAppointmentService(appointmentService, voidReason);
    }

    @Test
    public void shouldVoidTheAppointmentServiceBeIdempotent() throws Exception {
        String appointmentServiceUuid = "appointmentServiceUuid";
        String voidReason = "voidReason";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid(appointmentServiceUuid);
        appointmentService.setName("serviceName");
        appointmentService.setVoided(true);
        when(appointmentServiceService.getAppointmentServiceByUuid(appointmentServiceUuid)).thenReturn(appointmentService);
        when(appointmentServiceService.voidAppointmentService(appointmentService, voidReason)).thenReturn(appointmentService);

        appointmentServiceController.voidAppointmentService(appointmentServiceUuid, voidReason);

        verify(appointmentServiceService, times(1)).getAppointmentServiceByUuid(appointmentServiceUuid);
        verify(appointmentServiceService, times(0)).voidAppointmentService(appointmentService, voidReason);
    }
}