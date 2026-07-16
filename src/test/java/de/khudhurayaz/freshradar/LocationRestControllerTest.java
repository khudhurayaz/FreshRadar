package de.khudhurayaz.freshradar;

import de.khudhurayaz.freshradar.controller.api.LocationRestController;
import de.khudhurayaz.freshradar.dto.CreateLocationRequest;
import de.khudhurayaz.freshradar.dto.LocationRequest;
import de.khudhurayaz.freshradar.services.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LocationRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationRestController locationRestController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(locationRestController)
                .build();
    }

    @Test
    void showLocations_returnsLocationList() throws Exception {
        LocationRequest fridge = new LocationRequest();
        fridge.setId(1);
        fridge.setLocation("Kühlschrank");
        fridge.setAddedAt(Timestamp.valueOf("2026-07-16 12:00:00"));

        LocationRequest cupboard = new LocationRequest();
        cupboard.setId(2);
        cupboard.setLocation("Vorratsschrank");
        cupboard.setAddedAt(Timestamp.valueOf("2026-07-16 13:00:00"));

        when(locationService.getLocations())
                .thenReturn(List.of(fridge, cupboard));

        mockMvc.perform(get("/api/location/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].location").value("Kühlschrank"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].location").value("Vorratsschrank"));

        verify(locationService).getLocations();
    }

    @Test
    void addLocation_returns200_whenLocationIsSaved() throws Exception {
        when(locationService.addLocation(any(CreateLocationRequest.class)))
                .thenReturn(Optional.of(true));

        mockMvc.perform(put("/api/location/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "location": "Kühlschrank"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Lagerort erfolgreich gespeichert!"
                ));

        verify(locationService).addLocation(any(CreateLocationRequest.class));
    }

    @Test
    void addLocation_returns400_whenServiceReturnsFalse() throws Exception {
        when(locationService.addLocation(any(CreateLocationRequest.class)))
                .thenReturn(Optional.of(false));

        mockMvc.perform(put("/api/location/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "location": "Kühlschrank"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "Lagerort konnte nicht gespeichert werden!"
                ));

        verify(locationService).addLocation(any(CreateLocationRequest.class));
    }

    @Test
    void addLocation_returns400_whenServiceReturnsEmptyOptional() throws Exception {
        when(locationService.addLocation(any(CreateLocationRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/location/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "location": "Kühlschrank"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "Lagerort konnte nicht gespeichert werden!"
                ));

        verify(locationService).addLocation(any(CreateLocationRequest.class));
    }

    @Test
    void updateLocation_returnsSuccessMessage_whenServiceReturnsFalse() throws Exception {
        /*
         * Achtung: Die Controller-Logik ist umgekehrt:
         * false bedeutet hier "erfolgreich aktualisiert".
         */
        when(locationService.save(any(CreateLocationRequest.class), eq(1)))
                .thenReturn(false);

        mockMvc.perform(put("/api/location/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "location": "Gefrierschrank"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Location wurde erfolgreich aktualisiert!"
                ));

        verify(locationService).save(any(CreateLocationRequest.class), eq(1));
    }

    @Test
    void updateLocation_returnsFailureMessage_whenServiceReturnsTrue() throws Exception {
        /*
         * Achtung: Die Controller-Logik ist umgekehrt:
         * true bedeutet hier "konnte nicht aktualisiert werden".
         */
        when(locationService.save(any(CreateLocationRequest.class), eq(1)))
                .thenReturn(true);

        mockMvc.perform(put("/api/location/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "location": "Gefrierschrank"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Location konnte nicht aktualisiert werden!"
                ));

        verify(locationService).save(any(CreateLocationRequest.class), eq(1));
    }

    @Test
    void deleteLocation_returns200_whenLocationWasDeleted() throws Exception {
        when(locationService.delete(1))
                .thenReturn(true);

        mockMvc.perform(delete("/api/location/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Location wurde erfolgreich gelöscht!"
                ));

        verify(locationService).delete(1);
    }

    @Test
    void deleteLocation_returns404_whenLocationCannotBeDeleted() throws Exception {
        when(locationService.delete(99))
                .thenReturn(false);

        mockMvc.perform(delete("/api/location/delete/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(
                        "Location konnte nicht gelöscht werden!"
                ));

        verify(locationService).delete(99);
    }

    @Test
    void getLocationById_returns200AndLocation_whenLocationExists() throws Exception {
        LocationRequest location = new LocationRequest();
        location.setId(1);
        location.setLocation("Kühlschrank");
        location.setAddedAt(Timestamp.valueOf("2026-07-16 12:00:00"));

        when(locationService.getLocation(1))
                .thenReturn(Optional.of(location));

        mockMvc.perform(get("/api/location/get/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.location").value("Kühlschrank"));

        /*
         * Der Controller ruft getLocation(id) zweimal auf:
         * einmal für isPresent() und danach für get().
         */
        verify(locationService, org.mockito.Mockito.times(2))
                .getLocation(1);
    }

    @Test
    void getLocationById_returns404_whenLocationDoesNotExist() throws Exception {
        when(locationService.getLocation(99))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/location/get/99"))
                .andExpect(status().isNotFound());

        verify(locationService).getLocation(99);
    }
}