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
        DRAFT(1),
        TODO(2),
        DISCUSSION(3),
        GO(4),
        DONE(5),
        REVIEW(6),
        APPROVED(7),
        SEALED(8),
        DECLINE(9),
        CANCELED(10),
        ARCHIVE(11);

        int code;

        Status(int code) {
            this.code = code;
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
            return "[" + name() + ", id " + code + "]";
        }
    }
}
