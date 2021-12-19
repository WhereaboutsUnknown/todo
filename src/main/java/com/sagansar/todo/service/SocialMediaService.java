package com.sagansar.todo.service;

import com.sagansar.todo.model.work.Invite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service
public class SocialMediaService {

    private static final Logger logger = LoggerFactory.getLogger(SocialMediaService.class);

    private final SecurityService securityService;

    private final RestOperations restOperations;

    public SocialMediaService(SecurityService securityService) {
        this.securityService = securityService;
        this.restOperations = new RestTemplate();
    }

    public void sendTelegramInvite(@NonNull Invite invite) {
        //TODO: use pengrad Telegram Bot API for sending notifications with URLs for accepting invite

        //TODO: должна приходить ссылка (в тг?), по которой можно принять инвайт (секьюрити должна пропустить), InviteController
        logger.warn("Telegram notifications are still in progress!");

        ////////////////////////////////////

        String urlSecurityKey = securityService.generateUrlInviteKey(invite);

    }
}
