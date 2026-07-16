package de.khudhurayaz.freshradar.model;

import de.khudhurayaz.freshradar.repositories.UserRepository;
import de.khudhurayaz.freshradar.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute
    public void addAttributes(Model model, Authentication authentication) {
        model.addAttribute("appName", Constants.APP_NAME);
        model.addAttribute("version", Constants.VERSION);
        model.addAttribute("cssPath", Constants.CSS_PATH);
        model.addAttribute("mobilePath", Constants.CSS_PATH_MOBILE);
        model.addAttribute("jsPath", Constants.JAVASCRIPT_PATH);
        model.addAttribute("jsContactPath", Constants.JAVASCRIPT_CONTACT_PATH);
        model.addAttribute("jsDashboardPath", Constants.JAVASCRIPT_DASHBOARD_PATH);
        model.addAttribute("jsUtil", Constants.JAVASCRIPT_UTIL_PATH);
        model.addAttribute("appIcon", Constants.APP_ICON);
        // Author
        model.addAttribute("author", "Ayaz Khudhur");
        model.addAttribute("authorDescription", "Ayaz Khudhur ist ein Studen an der HSBI Minden und studiert Informatik.");

        //User
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> user = userRepository.findByEmail(authentication.getName());
            if (user.isPresent() && user.get().getSubscription() != null) {
                boolean aboPausiert = user.get().getSubscription().getStatus().equalsIgnoreCase("pause");
                model.addAttribute("aboPausiert", aboPausiert);
                model.addAttribute("aboBeendet", user.get().getSubscription().getStatus().equalsIgnoreCase("ended"));
                model.addAttribute("role", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse(null));

                boolean isAdmin = user.get().getRole().equalsIgnoreCase("admin");
                model.addAttribute("isAdmin", isAdmin);
            }
        }
    }
}

