package de.khudhurayaz.freshradar;

import de.khudhurayaz.freshradar.controller.api.ProductRestController;
import de.khudhurayaz.freshradar.dto.CategoryRequest;
import de.khudhurayaz.freshradar.dto.ProductRequest;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.repositories.UserRepository;
import de.khudhurayaz.freshradar.services.CategoryService;
import de.khudhurayaz.freshradar.services.LocationService;
import de.khudhurayaz.freshradar.services.ProductService;
import de.khudhurayaz.freshradar.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class RestApiProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "test@example.com")
    void getAllProducts_redirectsToSubscribe_whenUserHasNoSubscription() throws Exception {
        User user = new User();
        user.setId(1);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/product/all"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getFilteredProducts_redirectsToSubscribe_whenUserHasNoSubscription() throws Exception {
        User user = new User();
        user.setId(1);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/product/showProductsWithCategoryAndLocation")
                        .param("category", "2")
                        .param("location", "3"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void getProduct_returnsProduct_whenItExists() throws Exception {
        ProductRequest product = new ProductRequest();
        product.setId(7);
        product.setName("Käse");

        when(productService.getProduct(7))
                .thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/product/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Käse"));
    }

    @Test
    void getProduct_returns404_whenItDoesNotExist() throws Exception {
        when(productService.getProduct(999))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/product/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_returns200_whenItExists() throws Exception {
        ProductRequest deletedProduct = new ProductRequest();
        deletedProduct.setId(4);

        when(productService.delete(4))
                .thenReturn(Optional.of(deletedProduct));

        mockMvc.perform(delete("/api/product/delete/4"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProduct_returns404_whenItDoesNotExist() throws Exception {
        when(productService.delete(999))
                .thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/product/delete/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_returns400_whenCategoryIsInvalid() throws Exception {
        when(categoryService.findById(999))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/product/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 999,
                                  "locationId": 1,
                                  "isOpen": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Kategorie oder Lagerort ungültig."));
    }

    @Test
    void updateProduct_returns400_whenLocationIsInvalid() throws Exception {
        CategoryRequest category = new CategoryRequest();
        category.setName("MILCH");

        when(categoryService.findById(1))
                .thenReturn(Optional.of(category));

        when(locationService.findById(999))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/product/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 1,
                                  "locationId": 999,
                                  "isOpen": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Kategorie oder Lagerort ungültig."));
    }
}