package com.sagansar.todo;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.infrastructure.validation.Validator;
import com.sagansar.todo.service.UserDetailsServiceImpl;
import org.passay.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.util.StringUtils;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .and()
                .authorizeRequests()
                .antMatchers("/resources/**").permitAll()
                .antMatchers("/login*").permitAll()
                .antMatchers("/loginError*").permitAll()
                .antMatchers("/*.css", "/*.png").permitAll()
                .antMatchers("/test/*").permitAll() //TODO: for API testing, hide/delete later
                .antMatchers("/file-service/**").permitAll()
                .antMatchers("/invite*").permitAll()
                .antMatchers("/invite/api*").permitAll()
                .antMatchers("/error/**").permitAll()
                .antMatchers("/logout").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/authenticate")
                .successForwardUrl("/")
                .and()
                .logout()
                .logoutUrl("/logout")
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/login");
    }

    @Override
    public void configure(WebSecurity web) {
        web
                .ignoring()
                .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**","/vendor/**","/fonts/**");
    }

    @Bean
    public AuthenticationProvider daoAuthenticationProvider(UserDetailsServiceImpl userDetailsService) {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordGenerator passwordGenerator() {
        return new PasswordGenerator();
    }

    @Bean
    public Validator passwordValidator() {
        Rule length = new LengthRule(8, 32);
        Rule noWhitespaces = new WhitespaceRule();
        Rule upper = new CharacterRule(EnglishCharacterData.UpperCase, 1);
        Rule lower = new CharacterRule(EnglishCharacterData.LowerCase, 1);
        Rule digit = new CharacterRule(EnglishCharacterData.Digit, 1);
        Rule special = new CharacterRule(EnglishCharacterData.Special, 1);

        return new Validator() {

            private final PasswordValidator lengthValidator = new PasswordValidator(length);
            private final PasswordValidator whitespacesValidator = new PasswordValidator(noWhitespaces);
            private final PasswordValidator symbolValidator = new PasswordValidator(upper, lower, digit, special);

            @Override
            public void validate(String password) throws BadRequestException {
                if (!StringUtils.hasText(password)) {
                    throw new BadRequestException("Строка пароля пустая");
                }
                PasswordData passwordData = new PasswordData(password);
                if (!lengthValidator.validate(passwordData).isValid()) {
                    throw new BadRequestException("Длина пароля должна быть от 8 до 32 символов");
                }
                if (!whitespacesValidator.validate(passwordData).isValid()) {
                    throw new BadRequestException("Пароль не должен содержать пробелов");
                }
                if (!symbolValidator.validate(passwordData).isValid()) {
                    throw new BadRequestException("Пароль должен содержать хотя бы один символ каждого типа: в нижнем регистре, в верхнем регистре, специальный символ, цифру");
                }
            }
        };
    }
}
