package com.sagansar.todo.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.service.SecurityService;
import com.sagansar.todo.telegram.model.BotResponse;
import com.sagansar.todo.telegram.model.CustomBotCommand;
import com.sagansar.todo.telegram.model.TelegramChat;
import com.sagansar.todo.telegram.repository.BotResponseRepository;
import com.sagansar.todo.telegram.repository.CustomBotCommandRepository;
import com.sagansar.todo.telegram.repository.TelegramChatRepository;
import com.sagansar.todo.telegram.util.TelegramBotMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TelegramBotService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);

    private final TelegramBot bot;

    private final SecurityService securityService;

    private final TelegramChatRepository chatRepository;

    private final BotResponseRepository responseRepository;

    private final CustomBotCommandRepository customBotCommandRepository;

    private final Map<Long, TelegramChat> chatCache = new HashMap<>();

    private final Map<TelegramBotMessages, String> responseCache = new HashMap<>();

    private final Map<String, Function<TelegramChat, String>> commands = new HashMap<>();

    public TelegramBotService(TelegramBot bot,
                              SecurityService securityService,
                              TelegramChatRepository chatRepository,
                              BotResponseRepository responseRepository,
                              CustomBotCommandRepository customBotCommandRepository) {
        this.bot = bot;
        this.securityService = securityService;
        this.chatRepository = chatRepository;
        this.responseRepository = responseRepository;
        this.customBotCommandRepository = customBotCommandRepository;
    }

    public boolean sendMessage(String message, @NonNull User user, String url) {
        if (user.getContacts() == null) {
            logger.error("?? ???????????????????????? {} ?????? ???????????????????? ????????????????????!", user.getUsername());
            return false;
        }
        try {
            String telegramUsername = user.getContacts().getTelegram();
            if (!StringUtils.hasText(telegramUsername)) {
                logger.error("?? ???????????????????????? {} ???? ???????????? ?????????????? Telegram!", user.getUsername());
                return false;
            }
            TelegramChat chat = chatRepository.findDistinctByUsername(telegramUsername);
            if (chat == null) {
                logger.error("???????????? ?????? ?????????????? ?????????????????? ?????????????????? ???????????????????????? {}: ?????? ???? ????????????", user.getUsername());
                return false;
            }
            if (StringUtils.hasText(url)) {
                sendMessage(chat.getId(), message, url);
            } else {
                sendMessage(chat.getId(), message);
            }
            return true;
        } catch (Exception e) {
            logger.error("???????????? ?????? ?????????????? ?????????????????? ?????????????????? ???????????????????????? {}: {}", user.getUsername(), e.getMessage());
            return false;
        }
    }

    public void sentTestMessage(String message) {
        sendMessage(550923649L, message);
    }

    /**
     * Delete chats from cache if inactive for 5 minutes;
     */
    public void deleteInactiveChatsFromCache() {
        synchronized (chatCache) {
            Set<Long> inactive = chatCache.entrySet().stream()
                    .filter(entry -> isChatInactive(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            for (Long id : inactive) {
                deleteFromChatCache(id);
            }
        }
    }

    private boolean isChatInactive(TelegramChat chat) {
        if (chat == null) {
            return true;
        }
        LocalDateTime lastUpdate = chat.getLastUpdateTime();
        if (lastUpdate == null) {
            return true;
        }
        return LocalDateTime.now(ZoneId.systemDefault()).minus(5, ChronoUnit.MINUTES).isAfter(lastUpdate);
    }

    private void sendMessage(Long chatId, String message, String url) {
        if (StringUtils.hasText(url)) {
            if (url.contains("localhost")) {
                url = url.replace("localhost", "127.0.0.1"); //telegram bug: does not properly map localhost
            }
            message += "[???](" + url + ")";
        }
        sendMessage(chatId, message, 1, ParseMode.MarkdownV2);
    }

    private void sendMessage(Long chatId, String message) {
        sendMessage(chatId, message, 1, ParseMode.HTML);
    }

    private void sendMessage(Long chatId, String message, int messageId) {
        sendMessage(chatId, message, messageId, ParseMode.HTML);
    }

    private void sendMessage(Long chatId, String message, int messageId, ParseMode parseMode) {
        SendMessage request = new SendMessage(chatId, message)
                .parseMode(parseMode)
                .disableWebPagePreview(true)
                .disableNotification(true)
                .replyToMessageId(messageId)
                .replyMarkup(new ForceReply());
        /*SendResponse sendResponse = bot.execute(request);
        boolean ok = sendResponse.isOk();
        Message responseMessage = sendResponse.message();
        logger.info(responseMessage.text());*/

        bot.execute(request);
    }

    private void sendMessage(SendMessage message) {
        bot.execute(message);
    }

    private String response(TelegramBotMessages messageType) {
        if (responseCache.containsKey(messageType)) {
            return responseCache.get(messageType);
        } else {
            Optional<BotResponse> botResponse = responseRepository.findById(messageType.getId());
            if (botResponse.isPresent()) {
                String responseMessage = botResponse.get().getMessage();
                responseCache.put(messageType, responseMessage);
                return responseMessage;
            }
            logger.error("?????????????????????? ?????????? ?????? ??????????????????: {}", messageType.toString().toLowerCase());
            return responseCache.get(TelegramBotMessages.DEFAULT);
        }
    }

    /**
     * Process Telegram chat update
     *
     * @param update chat update
     */
    private void processUpdate(Update update) {
        Message message = update.message();
        if (message == null) {
            logger.error("???????????????????? {}: message is null", update.updateId());
            return;
        }
        Long chatId = message.chat().id();
        String username = message.chat().username();
        if (username == null) {
            username = message.chat().id().toString();
        }
        TelegramChat chat = getChat(chatId, username);
        String text = message.text();
        String result;
        if (isCommand(message)) {
            result = cmd(text, chat);
        } else {
            result = response(TelegramBotMessages.CANNOT_RECOGNIZE);
        }
        sendMessage(chatId, result, message.messageId());
    }

    private boolean isCommand(Message message) {
        MessageEntity[] entities = message.entities();
        if (entities == null) {
            return false;
        }
        for (MessageEntity entity : entities) {
            if (MessageEntity.Type.bot_command.equals(entity.type())) {
                return true;
            }
        }
        return false;
    }

    private TelegramChat getChat(Long chatId, String username) {
        if (chatCache.containsKey(chatId)) {
            return chatCache.get(chatId);
        }
        if (chatCache.isEmpty() && responseCache.isEmpty()) {
            initResponseCache();
        }
        TelegramChat chat = chatRepository.findById(chatId).orElse(storeChat(chatId, username));
        chatCache.put(chatId, chat);
        return chat;
    }

    private TelegramChat storeChat(Long chatId, String username) {
        TelegramChat chat = new TelegramChat();
        chat.setId(chatId);
        chat.setUsername(username);
        chat.setLastUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
        return chatRepository.save(chat);
    }

    private void deleteFromChatCache(Long chatId) {
        chatCache.remove(chatId);
    }

    private String performWorkerRegistration(TelegramChat chat) {
        String username = chat.getUsername();
        String password = securityService.generatePassword();
        if (securityService.registerWorkerFromTelegram(username, password)) {
            return "???? ???????????????????????????????? ?? ?????????????? TODO!" +
                    "\n?????? ??????????: " + username +
                    "\n?????? ????????????: " + password;
        }
        return "???????????? ?????? ?????????????????????? ????????????????????????!";
    }

    @PostConstruct
    public void init() {
        bot.setUpdatesListener(updates -> {
            Integer lastId = null;
            for (Update update : updates) {
                try {
                    processUpdate(update);
                    lastId = update.updateId();
                } catch (Exception e) {
                    logger.error("{} : {} {}", e, e.getMessage(), e.getCause());
                    e.printStackTrace();
                    return lastId == null ? UpdatesListener.CONFIRMED_UPDATES_NONE : lastId;
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        initResponseCache();
        initCommands();
    }

    private void initResponseCache() {
        responseCache.put(
                TelegramBotMessages.DEFAULT,
                responseRepository.findById(TelegramBotMessages.DEFAULT.getId()).orElse(new BotResponse() {
                    @Override
                    public String getMessage() {
                        return "?? ??????????????????, ??????-???? ?????????? ???? ??????. ???? ???? ???????????????????????? - ???????????????????? ???????????? ?????????????? ?????? ?????????????? ??????????????????, ?????????? ????????, ?????? ?????????????????? ???????? ??????????!";
                    }
                }).getMessage()
        );
        responseCache.put(
                TelegramBotMessages.UNKNOWN_COMMAND,
                responseRepository.findById(TelegramBotMessages.UNKNOWN_COMMAND.getId()).orElse(new BotResponse() {
                    @Override
                    public String getMessage() {
                        return "?? ??????????????????, ?? ???? ???????? ?????????? ??????????????. ???? ?????????????? ?????????????????????? ??????????????!";
                    }
                }).getMessage()
        );
        responseCache.put(
                TelegramBotMessages.ON_START,
                responseRepository.findById(TelegramBotMessages.ON_START.getId()).orElse(new BotResponse() {
                    @Override
                    public String getMessage() {
                        return "????????????????????????, ?? - ?????? ???????????? ??????-?????????????????? ?? ???????????????????? TODO! ?????????? ????????????, ?????? ?? ???????? ?????? ?????? ??????????????, ?????????????? /help";
                    }
                }).getMessage()
        );
        responseCache.put(
                TelegramBotMessages.ON_STOP,
                responseRepository.findById(TelegramBotMessages.ON_STOP.getId()).orElse(new BotResponse() {
                    @Override
                    public String getMessage() {
                        return "?? ??????????????????, ???? ???? ?????????????? ???????????????? ?????????????????????? ?? ?????????? ?????????????? ?? ???????????? ??????????????, ???????? ?? ????????????????. ???????? ???? ???????????????? ?????????? ???????????????? ??????????????????????, ???????????? ?????????????? ?????????????? /start. ???????????????? ?????? ??????!";
                    }
                }).getMessage()
        );
    }

    /*=========================================== CHAT BOT COMMANDS ===========================================*/

    private void initCommands() {
        reg(
                "/start",
                (chat) -> response(TelegramBotMessages.ON_START)
        );
        reg(
                "/stop",
                (chat) -> {
                    deleteFromChatCache(chat.getId());
                    if (chatCache.isEmpty()) {
                        responseCache.clear();
                    }
                    return response(TelegramBotMessages.ON_STOP);
                }
        );
        reg(
                "/help",
                (chat) -> response(TelegramBotMessages.HELP)
        );
        reg(
                "/signup",
                this::performWorkerRegistration
        );
    }

    private void reg(String command, Function<TelegramChat, String> action) {
        commands.put(command, action);
    }

    /**
     * Perform action on chat bot command
     *
     * @param command command
     * @param chat chat
     * @return action performing result
     */
    private String cmd(String command, TelegramChat chat) {
        if (commands.containsKey(command)) {
            return commands.get(command).apply(chat);
        }
        return processCustomCommand(command);
    }

    /*======================================= CUSTOM CHAT BOT COMMANDS =======================================*/

    /**
     * Process custom chat bot command
     *
     * @param command command
     * @return message for chat bot response
     */
    private String processCustomCommand(String command) {
        CustomBotCommand customBotCommand = customBotCommandRepository.findById(command).orElse(null);
        if (customBotCommand == null || !StringUtils.hasText(customBotCommand.getOutput())) {
            logger.error("????????????: ???? ?????????????????? ???????????????????????????????? ?????????????? {}", command);
            return response(TelegramBotMessages.UNKNOWN_COMMAND);
        }
        return customBotCommand.getOutput();
    }
}
