package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.InviteDto;
import com.sagansar.todo.controller.dto.TaskBasic;
import com.sagansar.todo.controller.mapper.InviteMapper;
import com.sagansar.todo.controller.mapper.TaskMapper;
import com.sagansar.todo.controller.util.RestResponse;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.work.Invite;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.InviteRepository;
import com.sagansar.todo.service.ArchiveService;
import com.sagansar.todo.service.InviteService;
import com.sagansar.todo.service.SecurityService;
import com.sagansar.todo.service.TodoService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@Transactional
@AllArgsConstructor
@ResponseBody
@RequestMapping(path = "/invite/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class InviteRestController {

    private final TodoService todoService;

    private final InviteService inviteService;

    private final ArchiveService archiveService;

    private final SecurityService securityService;

    private final InviteRepository inviteRepository;

    @GetMapping ("")
    public RestResponse processInviteAnswer(@RequestParam(name = "id") Long inviteId, @RequestParam(name = "accept") boolean accept) throws BadRequestException {
        Invite invite = inviteService.processInviteAnswer(inviteId, accept);
        Worker worker = invite.getWorker();
        if (worker == null) {
            throw new BadRequestException("Приглашение не адресовано исполнителю!");
        }
        if (!accept) {
            archiveService.increaseRejected(worker);
        }
        return new RestResponse("Ответ принят, " + (accept ? todoService.processAcceptedInvite(invite) : "Вы успешно отказались от задачи!"));
    }

    @GetMapping("/worker/{workerId}")
    public List<InviteDto> getAllInvites(@PathVariable Integer workerId) {
        securityService.getAuthorizedWorker(workerId);
        List<Invite> invites = inviteService.findWorkerInvites(workerId);
        return invites.stream()
                .map(InviteMapper::inviteToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/worker/{workerId}/{id}")
    public TaskBasic getTaskFromInvite(@PathVariable Integer workerId, @PathVariable Long id) throws BadRequestException {
        securityService.getAuthorizedWorker(workerId);
        Invite invite = inviteRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Приглашение не найдено!"));
        if (!workerId.equals(invite.getWorker().getId())) {
            throw new BadRequestException("Приглашение адресовано другому исполнителю!");
        }
        return TaskMapper.taskToBasic(invite.getTask());
    }
}
