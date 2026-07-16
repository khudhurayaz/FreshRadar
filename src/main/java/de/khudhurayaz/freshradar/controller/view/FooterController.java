package de.khudhurayaz.freshradar.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FooterController {

    @GetMapping("/impressum")
    public String impressum() {
        return "impressum";
    }

    @GetMapping("/privacy_policy")
    public String privacyPolicy() {
        return "privacy_policy";
    }

    @GetMapping("/disclaimer")
    public String disclaimer() {
        return "disclaimer";
    }
}
