package edu.mci.foodorderbuddy.security;

import edu.mci.foodorderbuddy.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Allow access to static resources
        http.authorizeHttpRequests(authorize ->
                authorize.requestMatchers("/images/**", "/icons/**").permitAll()
        );

        // Configure login page and success URL
        http.formLogin(formLogin ->
                formLogin
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/menu", true)
        );

        // Configure logout behavior
        http.logout(logout ->
                logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login")
        );

        // Configure Vaadin security
        super.configure(http);
        setLoginView(http, LoginView.class);
    }
}