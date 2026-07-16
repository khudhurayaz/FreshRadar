package de.khudhurayaz.freshradar;

import de.khudhurayaz.freshradar.controller.api.CategoryRestController;
import de.khudhurayaz.freshradar.dto.CategoryRequest;
import de.khudhurayaz.freshradar.repositories.UserRepository;
import de.khudhurayaz.freshradar.services.CategoryService;
import de.khudhurayaz.freshradar.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    /*
     * Wird vom CheckInterceptor benötigt, der über WebConfig geladen wird.
     */
    @MockitoBean
    private UserService userService;

    /*
     * Wird vom Bean globalModelAttributes benötigt.
     */
    @MockitoBean
    private UserRepository userRepository;

    @Test
    void showAllCategories_returns200AndCategoryList_whenCategoriesExist() throws Exception {
        CategoryRequest milk = new CategoryRequest();
        milk.setId(1);
        milk.setName("Milch");

        CategoryRequest fruit = new CategoryRequest();
        fruit.setId(2);
        fruit.setName("Obst");

        when(categoryService.findAll()).thenReturn(List.of(milk, fruit));

        mockMvc.perform(get("/api/category/show"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Milch"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Obst"));

        verify(categoryService).findAll();
    }

    @Test
    void showAllCategories_returns400_whenNoCategoriesExist() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/category/show"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));

        verify(categoryService).findAll();
    }

    @Test
    void addCategory_returns200_whenServiceSavesCategory() throws Exception {
        when(categoryService.add(any(CategoryRequest.class)))
                .thenReturn(Optional.of(true));

        mockMvc.perform(put("/api/category/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Milch"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Kategorie erfolgreich gespeichert!"));

        verify(categoryService).add(any(CategoryRequest.class));
    }

    @Test
    void addCategory_returns400_whenServiceReturnsFalse() throws Exception {
        when(categoryService.add(any(CategoryRequest.class)))
                .thenReturn(Optional.of(false));

        mockMvc.perform(put("/api/category/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Milch"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Kategorie konnte nicht gespeichert werden!"));

        verify(categoryService).add(any(CategoryRequest.class));
    }

    @Test
    void addCategory_returns400_whenServiceReturnsEmptyOptional() throws Exception {
        when(categoryService.add(any(CategoryRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/category/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Milch"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Kategorie konnte nicht gespeichert werden!"));

        verify(categoryService).add(any(CategoryRequest.class));
    }
}