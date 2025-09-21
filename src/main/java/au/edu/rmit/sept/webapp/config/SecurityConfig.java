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

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //so basically this is where we configure the security for our web app where we only 
        //allow certain pages to be accessed by anyone and other pages to be accessed only by logged in users
        http
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/", "/home", "/css/**", "/js/**", "/images/**", "/*.css", "/*.js", "/*.jpg", "/*.jpeg", "/*.png", "/*.gif", "/*.ico",  "/*.svg", "/static/**", "/error").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                .requestMatchers("/eventPage/**").authenticated() // Protected pages
                .requestMatchers("/organiser/**").hasAnyRole("ORGANISER", "ADMIN")// only organiser and admin can access organiser pages

                //only admin can access user management pages
                .requestMatchers("/users/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
            .successHandler(customAuthenticationSuccessHandler)
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


    //this we create some dummy users for this sprijnt to show the role base access control
    //next sprint we do no more dummy make it proper wth sign up and hash password and store in db
     @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username("dummy@example.com")
            .password(passwordEncoder().encode("password123"))
            .roles("USER")
            .build();

          UserDetails user2 = User.builder()
            .username("dummy2@example.com")
            .password(passwordEncoder().encode("password123"))
            .roles("USER")
            .build();

          UserDetails user3 = User.builder()
            .username("dummy3@example.com")
            .password(passwordEncoder().encode("password123"))
            .roles("USER")
            .build();

          UserDetails user4 = User.builder()
            .username("dummy4@example.com")
            .password(passwordEncoder().encode("password123"))
            .roles("USER")
            .build();


        UserDetails organiser = User.builder()
            .username("dummy5@example.com")
            .password(passwordEncoder().encode("password123"))
            .roles("ORGANISER")
            .build();

        UserDetails organiser2 = User.builder()
            .username("dummy6@example.com")
            .password(passwordEncoder().encode("password123"))
            .roles("ORGANISER")
            .build();

            UserDetails admin = User.builder()
            .username("dummy7@example.com")
            .password(passwordEncoder().encode("password123"))
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(user, user2 , user3, user4 ,organiser, organiser2 ,  admin);
    }
}
