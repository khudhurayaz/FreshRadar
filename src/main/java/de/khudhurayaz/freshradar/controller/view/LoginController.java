package de.khudhurayaz.freshradar.controller.view;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@AllArgsConstructor
@Controller
@Log4j2
public class LoginController {

    @GetMapping("/login")
    public String login(Authentication auth) {
        // Wenn der User schon da ist, leite ihn direkt zum Dashboard
        if (auth != null && auth.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        log.debug("Loginpage wird geladen...");
        return "login";
    }
}
