package com.sagansar.todo.telegram.workflow;

import com.sagansar.todo.telegram.TelegramBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TelegramBotServiceSchedule {

    @Autowired
    TelegramBotService telegramBotService;

    @Scheduled(fixedDelay = 600_000)
    public void scheduleChatCacheCleaning() {
        telegramBotService.deleteInactiveChatsFromCache();
    }
}
