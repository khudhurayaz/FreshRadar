package de.khudhurayaz.freshradar.controller.view.admin;

import de.khudhurayaz.freshradar.dto.ContactRequest;
import de.khudhurayaz.freshradar.dto.ProductRequest;
import de.khudhurayaz.freshradar.dto.ProfileRequest;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.model.setting.PagesSetting;
import de.khudhurayaz.freshradar.services.AdminService;
import de.khudhurayaz.freshradar.util.Util;
import de.khudhurayaz.freshradar.util.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Log4j2
@Controller
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public String home(Authentication auth,
                       Principal principal,
                       Model model,
                       @RequestParam(defaultValue = "0") Integer profilePage,
                       @RequestParam(required = false) Integer profilePageSize,
                       @RequestParam(defaultValue = "0") Integer contactPage,
                       @RequestParam(required = false) Integer contactPageSize,
                       @RequestParam(defaultValue = "0") Integer produktPage,
                       @RequestParam(required = false) Integer produktPageSize) {

        if (!Validation.hasRole(auth, "admin")) {
            return "redirect:/dashboard";
        }

        int currentAdminId = adminService.findAdminByEmail(principal.getName()).getId();
        PagesSetting settings = adminService.getPagesSetting(currentAdminId);

        int finalProfileSize = (profilePageSize != null) ? profilePageSize :
                (settings != null ? settings.getProfilePageSize() : 5);

        int finalProductSize = (produktPageSize != null) ? produktPageSize :
                (settings != null ? settings.getProductPageSize() : 5);

        int finalContactSize = (contactPageSize != null) ? contactPageSize :
                (settings != null ? settings.getContactPageSize() : 5);

        if (profilePageSize != null || produktPageSize != null || contactPageSize != null) {
            if (settings == null ||
                    finalProfileSize != settings.getProfilePageSize() ||
                    finalProductSize != settings.getProductPageSize() ||
                    finalContactSize != settings.getContactPageSize()) {

                adminService.savePagesSetting(currentAdminId, finalProfileSize, finalProductSize, finalContactSize);
            }
        }

        List<ProfileRequest> profiles = adminService.allProfiles(currentAdminId)
                .orElse(List.of())
                .stream()
                .filter(profile -> profile.getUser() != null)
                .filter(profile -> profile.getUser().getId() != currentAdminId)
                .toList();

        Page<ProfileRequest> profileRequestPage = Util.getPages(profilePage, finalProfileSize, profiles);
        Page<ProductRequest> productRequestPage = Util.getPages(produktPage, finalProductSize, adminService.allProducts().get());
        Page<ContactRequest> contactRequestsPage = Util.getPages(contactPage, finalContactSize, adminService.allContacts());

        model.addAttribute("profiles", profiles);
        model.addAttribute("profilePage", profileRequestPage);
        model.addAttribute("products", adminService.allProducts().get());
        model.addAttribute("productPage", productRequestPage);
        model.addAttribute("contacts", adminService.allContacts());
        model.addAttribute("contactPage", contactRequestsPage);
        model.addAttribute("allUsers", adminService.allUsers());
        model.addAttribute("allLocations", adminService.allLocations());
        model.addAttribute("allCategories", adminService.allCategories());

        model.addAttribute("profilePageSize", finalProfileSize);
        model.addAttribute("produktPageSize", finalProductSize);
        model.addAttribute("contactPageSize", finalContactSize);

        return "admin/admin";
    }

    @PutMapping("/profile/{profileId}")
    @ResponseBody
    public ResponseEntity<ProfileRequest> updateProfile(
            @PathVariable int profileId,
            @RequestBody ProfileRequest request){
        request.setId(profileId);
        User user = adminService.findAdminId(profileId);
        request.setUser(user);
        if (adminService.saveProfile(request).get()){
            log.debug("Profile saved...");
            return ResponseEntity.ok().build();
        } else {
            log.debug("Profile konnte nicht gespeichert werden....");
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/profile/{profileId}")
    @ResponseBody
    public ResponseEntity<Void> deleteProfile(@PathVariable int profileId) {
        boolean deleted = adminService.deleteProfileById(profileId);

        if (deleted) {
            log.debug("Profile erfolgreich gelöscht...");
            return ResponseEntity.ok().build();
        } else {
            log.debug("Profile konnte nicht gelöscht werden....");
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/product/{productId}")
    @ResponseBody
    public ResponseEntity<ProductRequest> updateProduct(
            @PathVariable int productId,
            @RequestBody ProductRequest request){
        request.setId(productId);
        log.debug("Product.userId: {}", request.getUserId());
        User user = adminService.findAdminId(request.getUserId());
        log.debug("user: {}", user.getEmail());
        request.setUserId(user.getId());
        if (adminService.saveProduct(request).get()){
            log.debug("Product saved...");
            return ResponseEntity.ok().build();
        } else {
            log.debug("Produkt konnte nicht gespeichert werden....");
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/product/{productId}")
    @ResponseBody
    public ResponseEntity<ProfileRequest> deleteProduct(
            @PathVariable int productId){
        Optional<ProductRequest> productRequestOptional = adminService.findProduct(productId);
        User user = null;
        if (productRequestOptional.isPresent()) {
            user = adminService.findAdminId(productRequestOptional.get().getUserId());
        }
        assert user != null;
        Optional<ProductRequest> request = adminService.getProductRequest(productId);
        log.debug("request zum löschen {}", request.get());
        if (adminService.deleteProduct(request.get()).get()) {
            log.debug("Produkt erfolgreich gelöscht...");
            return ResponseEntity.ok().build();
        } else {
            log.debug("Produkt konnte nicht gelöscht werden....");
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/message/{messageId}")
    @ResponseBody
    public ResponseEntity<ContactRequest> deleteMessage(
            @PathVariable int messageId){
        Optional<ContactRequest> contactRequestOptional = adminService.findMessage(messageId);
        if (contactRequestOptional.isEmpty())
            return ResponseEntity.notFound().build();

        log.debug("Lösche die Nachricht von '{}'", contactRequestOptional.get().getEmail());
        if (adminService.deleteMessage(contactRequestOptional.get())) {
            log.debug("Nachricht erfolgreich gelöscht...");
            return ResponseEntity.ok().build();
        } else {
            log.debug("NAchricht konnte nicht gelöscht werden....");
            return ResponseEntity.notFound().build();
        }
    }
}
