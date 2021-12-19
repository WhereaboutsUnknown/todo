package com.sagansar.todo.service;

import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.work.Invite;
import com.sagansar.todo.telegram.TelegramBot;
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

    private final TelegramBot telegramBot;

    private final RestOperations restOperations;

    public SocialMediaService(SecurityService securityService, TelegramBot telegramBot) {
        this.securityService = securityService;
        this.telegramBot = telegramBot;
        this.restOperations = new RestTemplate();
    }

    public void sendTelegramInvite(@NonNull Invite invite) {
        //TODO: use pengrad Telegram Bot API for sending notifications with URLs for accepting invite

        //TODO: должна приходить ссылка (в тг?), по которой можно принять инвайт (секьюрити должна пропустить), InviteController
        logger.warn("Telegram notifications are still in progress!");

        ////////////////////////////////////
        User user = invite.getWorker().getUser();

        String urlSecurityKey = securityService.generateUrlInviteKey(invite);
        final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String inviteUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment("invite")
                .queryParam("token", urlSecurityKey)
                .build()
                .toUriString();
        String message = generateInviteMessage(inviteUrl);
        telegramBot.sendMessage(message, user.getContacts());
    }

    private String generateInviteMessage(String inviteUrl) {
        return "Для Вас есть новая задача! Чтобы принять или отказаться, перейдите по ссылке: " + inviteUrl;
    }
}
