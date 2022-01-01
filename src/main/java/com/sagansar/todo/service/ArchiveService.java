package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.external.TaskEstimateTable;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.manager.Unit;
import com.sagansar.todo.model.work.*;
import com.sagansar.todo.repository.ArchivedTaskRepository;
import com.sagansar.todo.repository.WorkerArchivedTaskRepository;
import com.sagansar.todo.repository.WorkerGroupTaskRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ArchiveService {

    private final ArchivedTaskRepository archivedTaskRepository;

    private final WorkerArchivedTaskRepository workerArchivedTaskRepository;

    private final WorkerGroupTaskRepository workerGroupTaskRepository;

    public ArchiveService(ArchivedTaskRepository archivedTaskRepository,
                          WorkerArchivedTaskRepository workerArchivedTaskRepository,
                          WorkerGroupTaskRepository workerGroupTaskRepository) {
        this.archivedTaskRepository = archivedTaskRepository;
        this.workerArchivedTaskRepository = workerArchivedTaskRepository;
        this.workerGroupTaskRepository = workerGroupTaskRepository;
    }

    /**
     * Archive completed task
     *
     * @param task task
     * @param creator task creator
     * @param unit unit
     * @param estimateTables Worker estimates for task
     * @return archived task
     * @throws BadRequestException in case of null task
     */
    public TodoTask archiveTask(Manager manager, TodoTask task, Manager creator, Unit unit, List<TaskEstimateTable> estimateTables) throws BadRequestException {
        if (task == null) {
            throw new BadRequestException("Отсутствует задача для архивации!");
        }
        ArchivedTask archivedTask = createArchivedTask(task, manager);
        archivedTask.setUnit(unit);
        archivedTask.setCreator(creator);
        Map<Integer, Integer> estimates = estimateTables.stream()
                .collect(Collectors.toMap(TaskEstimateTable::getWorkerId, TaskEstimateTable::getEstimate));
        workerArchivedTaskRepository.saveAll(
                workerGroupTaskRepository.findAllByTaskId(task.getId()).stream()
                        .filter(Objects::nonNull)
                        .map(workerGroupTask -> createRelation(workerGroupTask, estimates.get(workerGroupTask.getWorker().getId())))
                        .collect(Collectors.toList())
        );
        archivedTask.setArchiveTime(LocalDateTime.now(ZoneId.systemDefault()));
        archivedTaskRepository.save(archivedTask);
        return task;
    }

    /**
     * Find archived task
     *
     * @param id task ID
     * @return archived task
     * @throws BadRequestException in case of invalid ID
     */
    public ArchivedTask getArchivedTask(Long id) throws BadRequestException {
        if (id == null) {
            throw new BadRequestException("Отсутствует ID архивной задачи!");
        }
        return archivedTaskRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Архивная задача не найдена!"));
    }

    private ArchivedTask createArchivedTask(@NonNull TodoTask task, @NonNull Manager manager) {
        ArchivedTask archivedTask = new ArchivedTask();
        archivedTask.setHeader(task.getHeader());
        archivedTask.setDescription(task.getDescription());
        archivedTask.setStack(task.getStack());
        archivedTask.setArchivedBy(manager);
        archivedTask.setCreationTime(task.getCreationTime());
        archivedTask.setSuccessful(task.getStatus() != null && (TodoStatus.Status.APPROVED.equals(task.getStatus().status())));
        archivedTask.setWorker(task.getWorker());
        return archivedTask;
    }

    private WorkerArchivedTask createRelation(@NonNull WorkerGroupTask workerGroupTask, Integer estimate) {
        WorkerArchivedTask workerArchivedTask = new WorkerArchivedTask();
        workerArchivedTask.setWorker(workerGroupTask.getWorker());
        workerArchivedTask.setTask(workerGroupTask.getTask());
        workerArchivedTask.setResponsible(workerGroupTask.isResponsible());
        workerArchivedTask.setEstimate(estimate);
        return workerArchivedTask;
    }
}
