package de.khudhurayaz.freshradar.controller.view;

import de.khudhurayaz.freshradar.controller.api.ProductRestController;
import de.khudhurayaz.freshradar.dto.CategoryRequest;
import de.khudhurayaz.freshradar.dto.ProductRequest;
import de.khudhurayaz.freshradar.model.Location;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.model.setting.PagesSetting;
import de.khudhurayaz.freshradar.repositories.LocationRepository;
import de.khudhurayaz.freshradar.services.LocationService;
import de.khudhurayaz.freshradar.services.ProductService;
import de.khudhurayaz.freshradar.services.UserService;
import de.khudhurayaz.freshradar.services.setting.PagesSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AppController {

    private final UserService userService;
    private final LocationRepository locationRepository;
    private final ProductService productService;
    private final LocationService locationService;
    private final PagesSettingService pagesSettingService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Principal principal,
            @RequestParam(name = "tab", defaultValue = "") String tab,
            @RequestParam(name = "category", required = false) Integer category,
            @RequestParam(name = "location", required = false) Integer location,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        return userService.findByEmail(principal.getName())
                .map(user -> buildDashboard(user, tab, category, location, model))
                .orElse("redirect:/logout");
    }

    private String buildDashboard(User user, String tab, Integer category, Integer location, Model model) {
        if (user.getSubscription() == null) {
            return "subscription-choose";
        }

        String subscriptionStatus = user.getSubscription().getStatus();
        if ("pause".equalsIgnoreCase(subscriptionStatus) || "ended".equalsIgnoreCase(subscriptionStatus)) {
            return "redirect:subscription";
        }

        int currentUserId = user.getId();
        List<CategoryRequest> categories = productService.getAllCategories();
        PagesSetting pagesSetting = pagesSettingService.getPagesSetting(currentUserId);

        int configuredSize = 5;
        if (pagesSetting != null && pagesSetting.getUserPageSize() > 0) {
            configuredSize = pagesSetting.getUserPageSize();
        }
        int safeSize = Math.min(configuredSize, 50);

        String selectedCategoryLabel = resolveCategoryLabel(category, categories);
        String selectedLocationName = resolveLocationName(location);

        Optional<List<ProductRequest>> productRequests = productService.findByUserId(currentUserId);
        boolean limitReached = productRequests.isPresent()
                && productRequests.get().size() >= ProductRestController.LIMIT;

        boolean isBasic = !user.getSubscription().getPlanType().equalsIgnoreCase("pro");

        model.addAttribute("planStatus", user.getSubscription().getStatus());
        model.addAttribute("isBasic", isBasic);
        model.addAttribute("limitReached", limitReached);
        model.addAttribute("profileImage",
                user.getProfile() != null
                        ? user.getProfile().getProfileImage()
                        : "assets/images/profile/default/defaultProfile.png");

        model.addAttribute("activeTab", tab);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedCategoryLabel", selectedCategoryLabel);
        model.addAttribute("selectedLocation", location);
        model.addAttribute("selectedLocationName", selectedLocationName);
        model.addAttribute("allCategories", categories);
        model.addAttribute("allLocations", locationRepository.findAll());
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("pagesSetting", pagesSetting);
        model.addAttribute("pageSize", safeSize);

        return "dashboard";
    }

    private String resolveCategoryLabel(Integer category, List<CategoryRequest> categories) {
        if (category == null) {
            return "Kategorie auswählen";
        }

        return categories.stream()
                .filter(cat -> cat.getId().equals(category))
                .map(CategoryRequest::getName)
                .findFirst()
                .orElse("Kategorie auswählen");
    }

    private String resolveLocationName(Integer location) {
        if (location == null) {
            return "Lagerort auswählen";
        }

        return locationService.findById(location)
                .map(Location::getLocation)
                .orElse("Lagerort auswählen");
    }
}