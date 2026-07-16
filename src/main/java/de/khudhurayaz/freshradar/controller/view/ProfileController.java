package de.khudhurayaz.freshradar.controller.view;

import de.khudhurayaz.freshradar.dto.ProfileRequest;
import de.khudhurayaz.freshradar.services.ProductService;
import de.khudhurayaz.freshradar.services.ProfileServices;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Optional;

@Controller
@AllArgsConstructor
@Log4j2
@RequestMapping("/profile")
public class ProfileController {

    private ProfileServices services;
    private ProductService productService;

    @GetMapping({"", "/"})
    public String profile(
            Principal principal,
            Model model
    ){

        Optional<ProfileRequest> profileRequestOptional = services.findByUserEmail(principal.getName());
        if (profileRequestOptional.isEmpty()) {
            return "redirect:/api/profile/edit";
        }
        profileRequestOptional.ifPresent(request -> {
            boolean isOwner = principal.getName().equals(request.getUser().getEmail());
            model.addAttribute("planStatus", request.getUser().getSubscription().getStatus());
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("profile", request);
            model.addAttribute("products", productService.countProductsByUser(request.getUser().getId()));
        });
       return "profile";
    }

    @GetMapping("/{profileId}")
    public String getProfile(@PathVariable int profileId, Principal principal, Model model) {
        ProfileRequest profile = services.findByUserId(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil nicht gefunden"));

        model.addAttribute("profile", profile);
        model.addAttribute("products", productService.countProductsByUser(profile.getUser().getId()));

        boolean isOwner = principal != null &&
                principal.getName().equals(profile.getUser().getEmail());
        model.addAttribute("isOwner", isOwner);

        return "profile";
    }

    @GetMapping("/getProfile")
    @ResponseBody
    public ProfileRequest getProfile(Principal principal) {
        Optional<ProfileRequest> profileRequestOptional = services.findByUserEmail(principal.getName());
        return profileRequestOptional.orElse(null);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateProfile(
            @PathVariable int id,
            @RequestBody ProfileRequest request
    ) {
        request.setId(id);
        request.setUser(request.getUser());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/edit")
    public String showProfileEdit(Model model, Principal principal) {
        model.addAttribute("cssPath", "/assets/style/master.css");
        model.addAttribute("mobilePath", "/assets/style/master.css");
        model.addAttribute("jsPath", "/assets/js/index.js");

        if (principal != null) {
            Optional<ProfileRequest> profileRequest = services.findByUserEmail(principal.getName());
            profileRequest.ifPresent(profile -> {
                model.addAttribute("profile", profile);
            });
        }
        return "profile-edit";
    }
}
