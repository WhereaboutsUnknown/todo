package com.sagansar.todo.model.work;

import com.sagansar.todo.model.worker.Worker;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "statistics")
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "points")
    private Double points;

    @Column(name = "done")
    private Integer done;

    @Column(name = "responsible")
    private Integer responsible;

    @Column(name = "failed")
    private Integer failed;

    @Column(name = "rejected")
    private Integer rejected;

    @OneToOne
    @JoinColumn(name = "worker")
    private Worker worker;
}
