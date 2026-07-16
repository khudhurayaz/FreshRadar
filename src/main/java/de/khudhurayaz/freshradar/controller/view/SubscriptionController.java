package de.khudhurayaz.freshradar.controller.view;

import de.khudhurayaz.freshradar.dto.subscription.ReactiveRequest;
import de.khudhurayaz.freshradar.dto.subscription.SubscriptionRequest;
import de.khudhurayaz.freshradar.model.Subscription;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.repositories.UserRepository;
import de.khudhurayaz.freshradar.services.SubscriptionService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@Log4j2
@AllArgsConstructor
public class SubscriptionController {

    private final UserRepository userRepository;
    private final SubscriptionService service;

    @PostMapping("/subscribe")
    public String processSubscription(
            @ModelAttribute SubscriptionRequest request,
            Principal principal,
            Model model) {
        User user = userRepository.findByEmail(principal.getName())
                .orElse(null);

        if (user == null) {
            model.addAttribute("error", "User not found!");
            return "redirect:/register";
        }
        if (user.getSubscription() == null) {
            request.setStatus("active");
            request.setPurchasedAt(Timestamp.valueOf(LocalDateTime.now()));

            Optional<Subscription> subscription = Optional.ofNullable(service.createSubscription(request, user));
            log.debug("Subscription aktiviert {}!", subscription.isPresent());
        }
        return "redirect:/profile/edit";
    }

    @GetMapping("/subscribe")
    public String showSubscription(Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow();

        if (user.getSubscription() == null) {
            return "redirect:/subscription-choose";
        }

        return "subscription";
    }

    @GetMapping("/subscription")
    public String showPauseScreen(
            Principal principal,
            Model model
    ){
        Optional<User> user = userRepository.findByEmail(principal.getName());
        if (user.isPresent()) {
            if (user.get().getSubscription() == null) {
                return "redirect:/subscription-choose";
            }

            // Eine aktivierte abo ist da
            model.addAttribute("subscriptionId", user.get().getSubscription().getId());
            model.addAttribute("userId", user.get().getId());
            model.addAttribute("planStatus", user.get().getSubscription().getStatus());
            model.addAttribute("planType", user.get().getSubscription().getPlanType());
            return "subscription";
        }
        return "login";
    }

    @PostMapping("/api/subscription/reactive")
    @ResponseBody
    public ResponseEntity<Void> reactiveSubscription(@RequestBody ReactiveRequest request) {
        log.debug("Abo wird für benutzer '{}' reaktiviert!", request.getUserId());
        log.debug("Status: {}", request.getStatus());
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setId(request.getSubscriptionId());
        subscriptionRequest.setUserId(request.getUserId());
        subscriptionRequest.setStatus(request.getStatus());

        service.save(subscriptionRequest);
        log.debug(subscriptionRequest.toString());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/subscription/upgradeOrDowngrade")
    @ResponseBody
    public ResponseEntity<Void> upgradeOrDowngrade(@RequestBody SubscriptionRequest request) {
        log.debug("Abo wird auf {} {}!", request.getPlanType(),
                (request.getPlanType().equalsIgnoreCase("pro") ? "Upgraded" : "Downgrade")
                );
        log.debug(request.toString());
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setId(request.getId());
        subscriptionRequest.setUserId(request.getUserId());
        subscriptionRequest.setPlanType(request.getPlanType());
        subscriptionRequest.setStatus(request.getStatus());

        service.save(subscriptionRequest);
        log.debug(subscriptionRequest.toString());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/subscription/ended")
    @ResponseBody
    public ResponseEntity<Void> endSubscription(
            @RequestParam Integer userId,
            @RequestParam Integer subscriptionId) {

        log.debug("Abo wird für Benutzer '{}' beendet!", userId);

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setId(subscriptionId);
        subscriptionRequest.setUserId(userId);
        subscriptionRequest.setStatus("cancelled");

        service.save(subscriptionRequest);

        return ResponseEntity.ok().build();
    }
}