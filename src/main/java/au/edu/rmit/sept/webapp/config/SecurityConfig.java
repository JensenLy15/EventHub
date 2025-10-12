package au.edu.rmit.sept.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import au.edu.rmit.sept.webapp.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenFailHandler customAuthenFailHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
                         CustomUserDetailsService customUserDetailsService,
                         CustomAuthenFailHandler customAuthenFailHandler) {
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthenFailHandler = customAuthenFailHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/", "/home", "/signup", "/login", "/css/**", "/js/**", "/images/**", 
                        "/*.css", "/*.js", "/*.jpg", "/*.jpeg", "/*.png", "/*.gif", "/*.ico", "/*.svg", 
                        "/static/**", "/error").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/eventPage/**").authenticated()
                .requestMatchers("/organiser/**").hasAnyRole("ORGANISER")
                .requestMatchers("/users/**").hasRole("ADMIN")

                //only admin can access admin pages
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(customAuthenticationSuccessHandler)
                .failureHandler(customAuthenFailHandler)
                // .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
            .userDetailsService(customUserDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}