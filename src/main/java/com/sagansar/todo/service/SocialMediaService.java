package com.sagansar.todo.service;

import com.sagansar.todo.model.general.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public void sendTelegramInvite(User user, String task) {
        //TODO: use pengrad Telegram Bot API for sending notifications with URLs for accepting invite

        //TODO: должна приходить ссылка (в тг?), по которой можно принять инвайт (секьюрити должна пропустить), InviteController
        logger.warn("Telegram notifications are still in progress!");

        ////////////////////////////////////
    }
}
