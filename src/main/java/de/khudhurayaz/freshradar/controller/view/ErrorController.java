package de.khudhurayaz.freshradar.controller.view;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

/**
 * Eigne Fehlerseiten für die Anwendung!
 * Es wird nur 404 und 405 behandelt!
 */
@ControllerAdvice
public class ErrorController {
    private final org.springframework.boot.webmvc.error.ErrorAttributes errorAttributes;

    public ErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        ServletWebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> attrs = errorAttributes.getErrorAttributes(webRequest,
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));
        Integer status = (Integer) attrs.getOrDefault("status", 500);
        String error = (String) attrs.getOrDefault("error", "Unknown");
        String message = (String) attrs.getOrDefault("message", "");
        String path = (String) attrs.getOrDefault("path", "");

        model.addAttribute("status", status);
        model.addAttribute("error", error);
        model.addAttribute("message", message);
        model.addAttribute("path", path);

        return "error";
    }
}