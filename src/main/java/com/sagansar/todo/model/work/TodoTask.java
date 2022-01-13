package com.sagansar.todo.model.work;

import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.manager.Unit;
import com.sagansar.todo.model.work.taskmeta.TaskAlert;
import com.sagansar.todo.model.work.taskmeta.TaskError;
import com.sagansar.todo.model.work.taskmeta.TaskInfo;
import com.sagansar.todo.model.work.taskmeta.TaskWarning;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.model.worker.WorkerResponse;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity(name = "todo_task")
public class TodoTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "header")
    private String header;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private Manager creator;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Manager manager;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private TodoStatus status;

    @OneToMany(mappedBy = "task")
    private Set<WorkerGroupTask> group;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<TaskFile> files;

    @OneToMany(mappedBy = "task")
    private List<Invite> invites;

    @OneToMany(mappedBy = "task")
    private List<WorkerResponse> responses;

    @Column(name = "main_stack")
    private String stack;

    @Column(name = "created")
    private LocalDateTime creationTime;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "visible_all")
    private boolean visibleToAll;

    @Column(name = "plan_start")
    private LocalDateTime plannedStart;

    @Column(name = "last_change")
    private LocalDateTime lastChangeDate;

    @Column(name = "task_history")
    private String history;

    public void setStatus(TodoStatus todoStatus) {
        if (todoStatus != null) {
            addHistoryRecord(
                    status == null ?
                            "Присвоен статус " + todoStatus.status() :
                            "Статус изменен с " + status.status() + " на " + todoStatus.status()
            );
            this.status = todoStatus;
        }
    }

    public void setDeadline(LocalDateTime deadline) {
        if (deadline != null) {
            addHistoryRecord(
                    this.deadline == null ?
                            "Установлен срок завершения: " + deadline :
                            "Срок завершения перенесен с " + this.deadline + " на " + deadline
            );
        } else {
            addHistoryRecord(
                    "Срок завершения удален"
            );
        }
        this.deadline = deadline;
    }

    public void setWorker(Worker worker) {
        if (worker != null) {
            addHistoryRecord("Назначен ответственный исполнитель: " + worker.getName());
        } else {
            addHistoryRecord("Ответственный исполнитель снят");
        }
        this.worker = worker;
    }

    public void setManager(Manager manager) {
        if (manager != null) {
            addHistoryRecord("Назначен куратор: " + manager.getFullName());
            this.manager = manager;
        }
    }

    public void addHistoryRecord(String record) {
        if (history == null) {
            history = "";
        }
        if (record != null) {
            history += (LocalDateTime.now(ZoneId.systemDefault()) + "  " + record + "[/]");
        }
    }

    public TaskAlert getAlert() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        TaskAlert deadlineAlert = deadlineAlert(now);
        TaskAlert startAlert = startAlert(now);
        TaskAlert tooLong = tooLongAlert(now);
        return deadlineAlert == null ? (startAlert == null ? tooLong : startAlert) : deadlineAlert;
    }

    public TaskAlert getResponsesInfo() {
        if (responses != null) {
            long unchecked = responses.stream()
                    .filter(response -> !response.isChecked())
                    .count();
            return unchecked > 0 ? new TaskInfo("Есть " + unchecked + " " + resolveCase(unchecked) + " на эту задачу!") : null;
        }
        return null;
    }

    public TaskAlert deadlineAlert(LocalDateTime now) {
        Set<TodoStatus.Status> done = Set.of(TodoStatus.Status.DONE, TodoStatus.Status.APPROVED, TodoStatus.Status.CANCELED, TodoStatus.Status.ARCHIVE);
        if (deadline == null || now.isBefore(deadline) || isStatusIn(done)) {
            return null;
        }
        return new TaskError("Срок завершения истек!");
    }

    public TaskAlert startAlert(LocalDateTime now) {
        Set<TodoStatus.Status> notStarted = Set.of(TodoStatus.Status.DRAFT, TodoStatus.Status.TODO, TodoStatus.Status.DISCUSSION);
        if (plannedStart == null || now.isBefore(plannedStart) || !isStatusIn(notStarted)) {
            return null;
        }
        return new TaskError("Плановый срок начала работы истек!");
    }

    public TaskAlert tooLongAlert(LocalDateTime now) {
        long days = 0;
        if (lastChangeDate != null) {
            days = ChronoUnit.DAYS.between(lastChangeDate, now);
        } else {
            return new TaskWarning("В задаче не было изменений с момента создания. Может быть, пора ее заархивировать?");
        }
        return days > 14 ? new TaskWarning("Последнее изменение было " + days + " дней назад. Возможно, пора заархивировать задачу?") : null;
    }

    private boolean isStatusIn(Set<TodoStatus.Status> statuses) {
        return status != null && statuses.contains(status.status());
    }

    private String resolveCase(long num) {
        long centtail = num % 100;
        long dectail = num % 10;
        if ((centtail > 9 && centtail < 21) || dectail == 0 || dectail > 4) {
            return "непросмотренных откликов";
        }
        return dectail == 1 ? "непросмотренный отклик" : "непросмотренных отклика";
    }

    @PrePersist
    public void onPersist() {
        lastChangeDate = LocalDateTime.now(ZoneId.systemDefault());
    }

    @PreUpdate
    public void onUpdate() {
        lastChangeDate = LocalDateTime.now(ZoneId.systemDefault());
    }
}
