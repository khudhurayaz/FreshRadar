package de.khudhurayaz.freshradar.config;

import de.khudhurayaz.freshradar.component.CustomAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(auth -> auth
                        // Öffentliche Endpunkte
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/impressum",
                                "/privacy_policy",
                                "/disclaimer",
                                "/error",
                                "/error/**",
                                "/assets/**",
                                "/assets/js/**",
                                "/assets/images/**",
                                "/assets/images/icons/**",
                                "/public/**"
                        ).permitAll()
                        // muss eingeloggt sein
                        .requestMatchers("/subscribe", "/dashboard/**").authenticated()
                        .requestMatchers("/register?").hasAuthority("subscribe")
                        .requestMatchers("/dashboard/**").hasAuthority("subscribe")
                        .requestMatchers("/admin").authenticated()
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers("/api/**").permitAll()
                        // Alles andere nur für eingeloggte Nutzer
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                // Login-Formular aktivieren
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successHandler)
                        .permitAll()
                )
                // Logout erlauben
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // CSRF-Schutz für API-Endpunkte deaktivieren
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/profile/**"))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/setting/**"))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/admin/**"));

        return http.build();
    }

    /**
     * @return Erstellt einen neuen BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Stellt den AuthenticationManager als Spring-Bean bereit.
     * Der AuthenticationManager wird benötigt, um Anmeldedaten
     * zu prüfen und den Nutzer zu authentifizieren.
     *
     * @param config die von Spring Security automatisch bereitgestellte
     *               AuthenticationConfiguration, aus der der Manager geholt wird
     * @return der konfigurierte AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}
