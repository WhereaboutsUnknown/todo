package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.PersonNameDto;
import com.sagansar.todo.controller.dto.TaskBasic;
import com.sagansar.todo.controller.dto.TaskFullDto;
import com.sagansar.todo.controller.dto.TaskShortDto;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.work.TodoStatus;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.model.work.WorkerGroupTask;
import com.sagansar.todo.model.work.taskmeta.TaskAccess;
import com.sagansar.todo.model.work.taskmeta.TaskAlert;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TaskMapper {

    public static TaskShortDto taskToShort(TodoTask task) {
        if (task == null) {
            return null;
        }
        TaskShortDto dto = new TaskShortDto();
        dto.setId(task.getId());
        dto.setHeader(task.getHeader());
        dto.setStack(task.getStack());
        dto.setDeadline(task.getDeadline());
        dto.setPerson(PersonMapper.managerToName(task.getManager()));
        TodoStatus status = task.getStatus();
        if (status != null) {
            dto.setStatus(status.getDescription());
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        dto.setAlert(task.deadlineAlert(now));
        return dto;
    }

    public static TaskFullDto taskToFull(TodoTask task) {
        if (task == null) {
            return null;
        }
        TaskFullDto dto = new TaskFullDto();
        dto.setId(task.getId());
        dto.setHeader(task.getHeader());
        dto.setDescription(task.getDescription());
        dto.setStack(task.getStack());
        dto.setDeadline(task.getDeadline());
        dto.setCreator(PersonMapper.managerToName(task.getCreator()));
        dto.setManager(PersonMapper.managerToName(task.getManager()));
        dto.setUnit(UnitMapper.unitToBasic(task.getUnit()));
        dto.setWorker(PersonMapper.workerToName(task.getWorker()));
        dto.setGroup(task.getGroup().stream()
                .map(WorkerGroupTask::getWorker)
                .map(PersonMapper::workerToName)
                .sorted(Comparator.comparing(PersonNameDto::getName))
                .collect(Collectors.toList()));
        dto.setFiles(task.getFiles().stream()
                .map(FileMapper::fileToBasic)
                .collect(Collectors.toList()));
        TodoStatus status = task.getStatus();
        if (status != null) {
            dto.setStatus(status.getDescription());
        }
        dto.setPlannedStart(task.getPlannedStart());
        dto.setHistory(task.getHistory());

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        dto.setAlerts(alerts(task.deadlineAlert(now)));

        return dto;
    }

    public static TaskShortDto taskToShort(TodoTask task, Manager manager, boolean supervisor) {
        TaskShortDto dto = taskToShort(task);
        dto.setPerson(PersonMapper.workerToName(task.getWorker()));

        if (hasAccess(task, manager) || supervisor) {
            dto.setResponsesInfo(task.getResponsesInfo());
            dto.setAlert(task.getAlert());
        }
        return dto;
    }

    public static TaskFullDto taskToFull(TodoTask task, Manager manager, boolean supervisor) {
        TaskFullDto dto = taskToFull(task);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

        if (hasAccess(task, manager) || supervisor) {
            dto.setAlerts(alerts(task.deadlineAlert(now), task.startAlert(now), task.tooLongAlert(now), task.getResponsesInfo()));
            dto.setInvited(task.getInvites().stream()
                    .filter(invite -> !invite.isAccepted())
                    .map(invite -> PersonMapper.workerToName(invite.getWorker()))
                    .sorted(Comparator.comparing(PersonNameDto::getName))
                    .collect(Collectors.toList()));
        }
        dto.setCurrentUserSupervisor(supervisor);
        TodoStatus.Status status = task.getStatus().status();
        dto.setEditWorkers(TaskAccess.REMOVE_WORKERS.hasAccess(status));
        dto.setInviteWorkers(TaskAccess.INVITE_WORKERS.hasAccess(status));
        dto.setCancelTask(TaskAccess.CANCEL_TASK.hasAccess(status));
        dto.setArchiveTask(TaskAccess.ARCHIVE_TASK.hasAccess(status));
        dto.setDeleteTask(TaskAccess.DELETE_TASK.hasAccess(status));
        return dto;
    }

    public static TaskBasic taskToBasic(TodoTask task) {
        if (task == null) {
            return null;
        }
        TaskBasic dto = new TaskBasic();
        dto.setId(task.getId());
        dto.setHeader(task.getHeader());
        TodoStatus status = task.getStatus();
        if (status != null) {
            dto.setStatus(status.getDescription());
        }
        dto.setDeadline(task.getDeadline());
        dto.setStack(task.getStack());
        dto.setDescription(task.getDescription());
        return dto;
    }

    private static boolean hasAccess(TodoTask task, Manager manager) {
        return manager.getId().equals(task.getManager().getId());
    }

    private static List<TaskAlert> alerts(TaskAlert... alerts) {
        return Arrays.stream(alerts)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
