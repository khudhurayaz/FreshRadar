package de.khudhurayaz.freshradar.controller.view;

import de.khudhurayaz.freshradar.dto.ChangePasswordRequest;
import de.khudhurayaz.freshradar.dto.ProfileRequest;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.model.setting.PagesSetting;
import de.khudhurayaz.freshradar.repositories.UserRepository;
import de.khudhurayaz.freshradar.services.ProfileServices;
import de.khudhurayaz.freshradar.services.setting.PagesSettingService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;

@Log4j2
@Controller
@AllArgsConstructor
@RequestMapping("/setting")
public class UserSettingController {

    private final ProfileServices services;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PagesSettingService pagesSettingService;

    @GetMapping
    public String setting(Principal principal, Model model) {
        Optional<ProfileRequest> profileRequestOptional = services.findByUserEmail(principal.getName());
        int userId;
        PagesSetting setting = null;
        if (profileRequestOptional.isPresent()) {
            userId = profileRequestOptional.get().getUser().getId();
            setting = pagesSettingService.getPagesSetting(userId);
            if (setting == null) {
                setting = new PagesSetting();
                setting.setUserPageSize(5);
            }
        }

        // model befüllen
        model.addAttribute("profile", profileRequestOptional.orElse(new ProfileRequest()));
        model.addAttribute("pagesSetting", setting);
        model.addAttribute("nextMonthFirstDay", LocalDate.now().plusMonths(1).withDayOfMonth(1));
        return "user-setting";
    }

    @PostMapping("/pagination")
    public ResponseEntity<String> savePaginationSetting(
            Principal principal,
            @RequestParam(name = "globalPageSize", defaultValue = "5") int globalPageSize) {

        log.debug("PaginationSize ist: {}", globalPageSize);
        Optional<ProfileRequest> profileRequestOptional = services.findByUserEmail(principal.getName());

        if (profileRequestOptional.isPresent()) {
            int userId = profileRequestOptional.get().getUser().getId();
            int safeSize = Math.min(Math.max(globalPageSize, 5), 50);

            pagesSettingService.savePagesSetting(userId, safeSize);
            return ResponseEntity.ok().body("Pagination wurde erfolgreich gespeichert!");
        }
        //"redirect:/setting"
        return ResponseEntity.badRequest().body("Konnte Pagination nicht speichern!");
    }

    @PostMapping("/changePassword")
    @ResponseBody
    public String changePassword(
            @RequestBody ChangePasswordRequest request,
            Principal principal,
            HttpServletResponse response, Model model) {

        // 2. Passwort-Validierung (Kriterien einzeln prüfen)
        boolean isValid = true;

        if (request.getNewPassword().length() < 12 || request.getRepeatPassword().length() < 12) {
            model.addAttribute("errorLength", "Mindestens 12 Zeichen");
            isValid = false;

        }
        if (!request.getNewPassword().matches(".*[A-Z].*") || !request.getRepeatPassword().matches(".*[A-Z].*")) {
            model.addAttribute("errorUpper", "Mindestens 1 Großbuchstabe");
            isValid = false;
        }
        if (!request.getNewPassword().matches(".*[a-z].*") || !request.getRepeatPassword().matches(".*[a-z].*")) {
            model.addAttribute("errorLower", "Mindestens 1 Kleinbuchstabe");
            isValid = false;
        }
        if (!request.getNewPassword().matches(".*[0-9].*") ||  !request.getRepeatPassword().matches(".*[0-9].*")) {
            model.addAttribute("errorDigit", "Mindestens 1 Ziffer");
            isValid = false;
        }
        if (!request.getNewPassword().matches(".*[@#$%^&+=!-].*") || !request.getRepeatPassword().matches(".*[@#$%^&+=!-].*")) {
            model.addAttribute("errorSpecial", "Mindestens 1 Sonderzeichen (@#$%^&+=!-)");
            isValid = false;
        }
        if (!request.getNewPassword().equals(request.getRepeatPassword())) {
            model.addAttribute("error", "Neue Passwörter stimmen nicht überein!");
            isValid = false;
        }

        if (!isValid) {
            model.addAttribute("error", "Das Passwort entspricht nicht den Sicherheitsrichtlinien.");
            model.addAttribute("errorPassword", "Das Passwort entspricht nicht den Sicherheitsrichtlinien.");
            response.setStatus(HttpStatus.BAD_REQUEST.value());

        }

        User user = services.findByUserEmail(principal.getName())
                .orElseThrow()
                .getUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            model.addAttribute("error", "Altes Passwort ist falsch!");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "user-setting";
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            model.addAttribute("error", "Neues Passwort und altes Passwort sind identisch!");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "user-setting";
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User user1 = userRepository.save(user);
        if (user1 != null) {
            response.setStatus(HttpStatus.OK.value());
            return "user-setting";
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            model.addAttribute("error", "Ein Fehler ist aufgetreten und das Passwort konnte nicht geändert werden!");
            return "user-setting";
        }
    }
}
