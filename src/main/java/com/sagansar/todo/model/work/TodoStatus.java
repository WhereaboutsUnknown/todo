package com.sagansar.todo.model.work;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "todo_status")
public class TodoStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_code")
    private Integer code;

    @Column(name = "status_name")
    private String statusName;

    @Column(name = "descr")
    private String description;

    public Status status() {
        Status status = TodoStatus.Status.fromCode(code);
        return status == null ? TodoStatus.Status.valueOf(statusName) : status;
    }

    @Getter
    public enum Status {
        DRAFT(1, "Черновик"),
        TODO(2, "Есть работа!"),
        DISCUSSION(3, "Открыто обсуждение"),
        GO(4, "В работе"),
        DONE(5, "Готово"),
        REVIEW(6, "На рассмотрении"),
        APPROVED(7, "Одобрено"),
        SEALED(8, "Задача закрыта"),
        DECLINE(9, "Не принято"),
        CANCELED(10, "Задача снята"),
        ARCHIVE(11, "Задача в архиве");

        private final int code;
        private final String alias;

        Status(int code, String alias) {
            this.code = code;
            this.alias = alias;
        }

        static Status fromCode(int code) {
            for (Status status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return DRAFT;
        }

        static Integer codeOf(String name) {
            return valueOf(name).code;
        }

        @Override
        public String toString() {
            return "[" + alias + ", code " + code + "]";
        }
    }
}
