package de.khudhurayaz.freshradar.controller.view;

import de.khudhurayaz.freshradar.dto.ProfileRequest;
import de.khudhurayaz.freshradar.model.Profile;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.repositories.UserRepository;
import de.khudhurayaz.freshradar.services.AutoLoginService;
import de.khudhurayaz.freshradar.services.ProfileServices;
import de.khudhurayaz.freshradar.services.UserService;
import de.khudhurayaz.freshradar.util.Validation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;

@AllArgsConstructor
@Controller
@Log4j2
public class RegisterController {

    private final UserService userService;
    private final PasswordEncoder  passwordEncoder;
    private final AutoLoginService autoLoginService;
    private final ProfileServices profileServices;

    @GetMapping("/register")
    public String showRegisterPage(
            @RequestParam(value = "subscribe", required = false) String subscribe,
            Authentication authentication, HttpSession session
    ){
        if (subscribe != null) {
            String email = (String) session.getAttribute("registeredEmail");

            User user = userService.findByEmail(email)
                    .orElse(null);
            if (user == null && email != null) {
                return "subscription-choose";
            }
        }
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            return userService.findByEmail(authentication.getName())
                    .map(user -> {
                        if (user.getSubscription() == null) {
                            return "subscription-choose";
                        } else {
                            return "redirect:/dashboard";
                        }
                    })
                    .orElse("register");
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {

        //Damit der benutzer sein E-Mail und passwort nicht nochmal eingeben muss, werden dies zurückgeschickt!
        model.addAttribute("email", email);
        model.addAttribute("password", password);

        // E-Mail check
        boolean isValidEmail = true;
        if (!Validation.patternMatches(email, Validation.EMAIL_PATTERN)) {
            model.addAttribute("error", "'" + email + "' ist keine gültige E-Mail-Adresse.");
            isValidEmail = false;
        }
        if (userService.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Diese E-Mail ist bereits registriert.");
            isValidEmail = false;
        }

        if (!isValidEmail) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "register";
        }

        // 2. Passwort-Validierung (Kriterien einzeln prüfen)
        boolean isValid = true;
        if (password.length() < 12) {
            model.addAttribute("errorLength", "Mindestens 12 Zeichen");
            isValid = false;
        }
        if (!password.matches(".*[A-Z].*")) {
            model.addAttribute("errorUpper", "Mindestens 1 Großbuchstabe");
            isValid = false;
        }
        if (!password.matches(".*[a-z].*")) {
            model.addAttribute("errorLower", "Mindestens 1 Kleinbuchstabe");
            isValid = false;
        }
        if (!password.matches(".*[0-9].*")) {
            model.addAttribute("errorDigit", "Mindestens 1 Ziffer");
            isValid = false;
        }
        if (!password.matches(".*[@#$%^&+=!-].*")) {
            model.addAttribute("errorSpecial", "Mindestens 1 Sonderzeichen (@#$%^&+=!-)");
            isValid = false;
        }

        if (!isValid) {
            model.addAttribute("error", "Das Passwort entspricht nicht den Sicherheitsrichtlinien.");
            model.addAttribute("errorPasswort", "Das Passwort entspricht nicht den Sicherheitsrichtlinien.");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "register";
        }

        try {
            User user = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .lastLoginAt(Timestamp.valueOf(LocalDateTime.now()))
                        .build();


            userService.save(user);
            ProfileRequest profile = new ProfileRequest();
            profile.setUser(user);
            profileServices.save(profile);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            session.setAttribute("registeredEmail", email);
            autoLoginService.login(
                    email, password, request
            );
            return "redirect:/register?subscribe";
        } catch (Exception e) {
            model.addAttribute("error", "Registrierung fehlgeschlagen");
            e.printStackTrace();
            return "register";
        }
    }
}
