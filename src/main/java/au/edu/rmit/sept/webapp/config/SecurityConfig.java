package au.edu.rmit.sept.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //so basically this is where we configure the security for our web app where we only 
        //allow certain pages to be accessed by anyone and other pages to be accessed only by logged in users
        http
            .authorizeHttpRequests((requests) -> requests
                // .requestMatchers("/", "/home", "/styles.css", "/favicon.svg", "/static/**", "/error", "/h2-console/**", "/*.jpg", "/*.jpeg").permitAll() // Public pages
                .requestMatchers("/", "/home", "/css/**", "/js/**", "/images/**", "/*.css", "/*.js", "/*.jpg", "/*.jpeg", "/*.png", "/*.gif", "/*.ico",  "/*.svg", "/static/**", "/error", "/h2-console/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                .requestMatchers("/eventPage/**").authenticated() // Protected pages
                .requestMatchers("/organiser/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .permitAll() // Use default Spring Security login form
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/") // Redirect to home page after logout
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**") // Needed for H2 console
            )

              .headers(headers -> headers.frameOptions().sameOrigin()  // Add this line
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username("user")
            .password(passwordEncoder().encode("password"))
            .roles("USER")
            .build();

        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin"))
            .roles("ADMIN", "USER")
            .build();

        return new InMemoryUserDetailsManager(user, admin);
    }
}
