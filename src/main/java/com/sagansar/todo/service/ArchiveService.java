package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.external.TaskEstimateTable;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.manager.Unit;
import com.sagansar.todo.model.work.*;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.*;
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

    private static final int ARCHIVED = TodoStatus.Status.ARCHIVE.getCode();

    private final ArchivedTaskRepository archivedTaskRepository;

    private final WorkerArchivedTaskRepository workerArchivedTaskRepository;

    private final WorkerGroupTaskRepository workerGroupTaskRepository;

    private final StatisticsRepository statisticsRepository;

    private final TodoStatusRepository statusRepository;

    public ArchiveService(ArchivedTaskRepository archivedTaskRepository,
                          WorkerArchivedTaskRepository workerArchivedTaskRepository,
                          WorkerGroupTaskRepository workerGroupTaskRepository,
                          StatisticsRepository statisticsRepository,
                          TodoStatusRepository statusRepository) {
        this.archivedTaskRepository = archivedTaskRepository;
        this.workerArchivedTaskRepository = workerArchivedTaskRepository;
        this.workerGroupTaskRepository = workerGroupTaskRepository;
        this.statisticsRepository = statisticsRepository;
        this.statusRepository = statusRepository;
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
        List<WorkerArchivedTask> workers = workerGroupTaskRepository.findAllByTaskId(task.getId()).stream()
                .filter(Objects::nonNull)
                .map(workerGroupTask -> createRelation(archivedTask, workerGroupTask, estimates.get(workerGroupTask.getWorker().getId())))
                .collect(Collectors.toList());
        workerArchivedTaskRepository.saveAll(workers);
        archivedTask.setArchiveTime(LocalDateTime.now(ZoneId.systemDefault()));
        archivedTaskRepository.save(archivedTask);
        calculateStatistics(workers.stream()
                .map(WorkerArchivedTask::getWorker)
                .collect(Collectors.toList()));

        task.setStatus(archived());
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

    public Statistics getWorkerStatistics(@NonNull Integer workerId) {
        return statisticsRepository.findDistinctByWorkerId(workerId);
    }

    public List<Statistics> getStatistics(@NonNull List<Integer> ids) {
        return statisticsRepository.findAllByWorkerIdIn(ids);
    }

    /**
     * Increase counter of rejected tasks
     *
     * @param worker Worker
     */
    public void increaseRejected(@NonNull Worker worker) {
        Statistics statistics = getWorkerStatistics(worker.getId());
        if (statistics == null) {
            statistics = new Statistics();
            statistics.setWorker(worker);
        }
        int rejected = statistics.getRejected() == null ? 0 : statistics.getRejected();
        statistics.setRejected(rejected + 1);
        statisticsRepository.save(statistics);
    }

    /**
     * Recalculate statistics for Workers
     *
     * @param workers Workers list
     */
    public void calculateStatistics(@NonNull List<Worker> workers) {
        statisticsRepository.saveAll(workers.stream()
                .map(this::calculateStatistics)
                .collect(Collectors.toList()));
    }

    /**
     * Recalculate statistics for Worker
     *
     * @param worker Worker
     * @return calculated statistics
     */
    private Statistics calculateStatistics(@NonNull Worker worker) {
        int tasksCount = archivedTaskRepository.countAllByWorkerId(worker.getId());
        int successCount = archivedTaskRepository.countAllByWorkerIdAndSuccessfulTrue(worker.getId());
        int failedCount = tasksCount - successCount;

        List<WorkerArchivedTask> tasks = workerArchivedTaskRepository.findAllByWorkerId(worker.getId());
        int responsible = 0;
        double pointsTotal = 0.0;
        for (WorkerArchivedTask task : tasks) {
            if (task.isResponsible()) {
                responsible++;
            }
            pointsTotal += task.getEstimate();
        }
        Statistics statistics = getWorkerStatistics(worker.getId());
        if (statistics == null) {
            statistics = new Statistics();
            statistics.setWorker(worker);
        }
        statistics.setCalculatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        statistics.setDone(successCount);
        statistics.setFailed(failedCount);
        statistics.setResponsible(responsible);
        statistics.setPoints(pointsTotal / tasks.size());
        return statistics;
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

    private WorkerArchivedTask createRelation(@NonNull ArchivedTask archivedTask, @NonNull WorkerGroupTask workerGroupTask, Integer estimate) {
        WorkerArchivedTask workerArchivedTask = new WorkerArchivedTask();
        workerArchivedTask.setWorker(workerGroupTask.getWorker());
        workerArchivedTask.setTask(archivedTask);
        workerArchivedTask.setResponsible(workerGroupTask.isResponsible());
        workerArchivedTask.setEstimate(estimate);
        return workerArchivedTask;
    }

    private TodoStatus archived() {
        return statusRepository.findById(ARCHIVED).orElseThrow(() -> new RuntimeException("Статус архивных задач не найден"));
    }
}
