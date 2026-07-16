package de.khudhurayaz.freshradar.interceptor;

import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.repositories.UserRepository;
import de.khudhurayaz.freshradar.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/**
 * Durch die Klasse <span>SubscriptionCheckInterceptor</span> kann ich ganz
 * einfach Testen, ob der Benutzer ein Abo hat, falls nicht wird weitergeleitet
 * auf {@link /de.khudhurayaz.freshradar.controller.view.RegisterController?subscribe}.
 * Ansonsten wird das Dashboard geladen.
 *
 * @author AyazKhudhur
 * @version 1.0.0
 */
@Log4j2
@Component
public class CheckInterceptor implements HandlerInterceptor {
    private final UserService userService;

    public CheckInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Mit dieser if-Abfrage erlaube ich die Öffentliche Pfade.
        if (
            uri.equals("/") ||
            uri.startsWith("/error") ||
            uri.equals("/login") ||
            uri.equals("/register") ||
            uri.equals("/logout") ||
            uri.equals("/subscribe") ||
            uri.startsWith("/assets")
        ) {
            return true;
        }

        // Auth prüfen, ob der benutzer eingeloggt ist
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !Objects.equals(auth.getPrincipal(), "anonymousUser")) {

            // Hole ich mir aus der Datenbank, wenn es ein treffer gibt.
            User user = userService.findByEmail(auth.getName()).orElse(null);

            // Wenn der User kein Abo hat und NICHT bereits auf der Abo-Seite ist:
            if (user != null && user.getSubscription() == null) {
                if (!uri.contains("/register") || (request.getQueryString() == null || !request.getQueryString().contains("subscribe"))) {
                    response.sendRedirect("/register?subscribe");
                    return false;
                }
            }
        }

        return true;
    }
}