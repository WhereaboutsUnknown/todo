package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.NotificationMessage;
import com.sagansar.todo.controller.mapper.NotificationMapper;
import com.sagansar.todo.controller.util.RestResponse;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.repository.NotificationRepository;
import com.sagansar.todo.service.NotificationService;
import com.sagansar.todo.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Transactional
@AllArgsConstructor
@ResponseBody
@RequestMapping(path = "/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
public class NotificationController {

    NotificationService notificationService;

    NotificationRepository notificationRepository;

    SecurityService securityService;

    @GetMapping("")
    public List<NotificationMessage> getNotifications() {
        User currentUser = securityService.getCurrentUser();
        Sort sort = Sort.by(Sort.Direction.DESC, "fireTime");
        return notificationRepository.findAllByUserId(currentUser.getId(), sort).stream()
                .map(NotificationMapper::notificationToMessage)
                .collect(Collectors.toList());
    }

    @PutMapping("")
    public RestResponse markReadNotifications(@RequestBody List<Long> ids) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        notificationRepository.findAllById(ids).stream()
                .filter(notification -> notification.getReadTime() == null)
                .forEach(notification -> notification.setReadTime(now));
        return new RestResponse("Уведомления помечены, как прочтенные");
    }
}
