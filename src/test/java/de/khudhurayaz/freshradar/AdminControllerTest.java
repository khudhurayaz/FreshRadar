package de.khudhurayaz.freshradar;

import de.khudhurayaz.freshradar.controller.view.admin.AdminController;
import de.khudhurayaz.freshradar.repositories.UserRepository;
import de.khudhurayaz.freshradar.services.AdminService;
import de.khudhurayaz.freshradar.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void deleteProfile_returnsOk_whenDeleted() throws Exception {
        when(adminService.deleteProfileById(12)).thenReturn(true);

        mockMvc.perform(delete("/admin/profile/12")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProfile_returnsBadRequest_whenDeleteFails() throws Exception {
        when(adminService.deleteProfileById(12)).thenReturn(false);

        mockMvc.perform(delete("/admin/profile/12")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}