package com.sagansar.todo;

import com.sagansar.todo.telegram.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocialMediaConfiguration {

    @Bean
    TelegramBot telegramBot() {
        return new TelegramBot();
    }
}
