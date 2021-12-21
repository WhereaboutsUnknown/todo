package com.sagansar.todo.service;

import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.work.Invite;
import com.sagansar.todo.telegram.TelegramBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SocialMediaService {

    private static final Logger logger = LoggerFactory.getLogger(SocialMediaService.class);

    private final SecurityService securityService;

    private final TelegramBotService telegramBotService;

    private final RestOperations restOperations;

    public SocialMediaService(SecurityService securityService, TelegramBotService telegramBotService) {
        this.securityService = securityService;
        this.telegramBotService = telegramBotService;
        this.restOperations = new RestTemplate();
    }

    /**
     * Send invite url by Telegram bot
     *
     * @param invite task invite
     */
    public boolean sendTelegramInvite(@NonNull Invite invite) {
        User user = invite.getWorker().getUser();

        String urlSecurityKey = securityService.generateUrlInviteKey(invite);
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String inviteUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment("invite")
                .queryParam("token", urlSecurityKey)
                .build()
                .toUriString();
        String message = generateInviteMessage();
        return telegramBotService.sendMessage(message, user, inviteUrl);
    }

    private String generateInviteMessage() {
        return "Для Вас есть новая задача! Чтобы принять или отказаться, перейдите по ссылке: ";
    }
}
