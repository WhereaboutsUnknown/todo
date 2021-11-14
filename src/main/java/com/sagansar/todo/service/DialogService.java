package com.sagansar.todo.service;

import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.work.Dialog;
import com.sagansar.todo.model.work.Message;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.repository.DialogRepository;
import com.sagansar.todo.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class DialogService {

    private static final Logger logger = LoggerFactory.getLogger(DialogService.class);

    private final MessageRepository messageRepository;

    private final DialogRepository dialogRepository;

    public DialogService(MessageRepository messageRepository,
                         DialogRepository dialogRepository) {
        this.messageRepository = messageRepository;
        this.dialogRepository = dialogRepository;
    }

    public void createDialog(@NonNull TodoTask task, @NonNull User user, String startMessage) {
        Dialog existingDialog = dialogRepository.findByTaskId(task.getId());
        if (existingDialog != null) {
            throw new RuntimeException("Обсуждение уже существует: задача [" + task.getId() + "]");
        }
        Dialog newDialog = createNewDialog(task);
        sendMessage(newDialog, user, startMessage);
    }

    private Dialog createNewDialog(@NonNull TodoTask task) {
        Dialog dialog = new Dialog();
        dialog.setTask(task);
        return dialogRepository.save(dialog);
    }

    private void sendMessage(Dialog dialog, User sender, String text) {
        if (dialog == null || sender == null) {
            throw new RuntimeException("Сообщение должно относиться к обсуждению и иметь отправителя");
        }
        Message message = new Message();
        message.setDialog(dialog);
        message.setUser(sender);
        message.setText(text);
        message.setTime(LocalDateTime.now(ZoneId.systemDefault()));
        messageRepository.save(message);
    }
}
