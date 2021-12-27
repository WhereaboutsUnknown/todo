package com.sagansar.todo.infrastructure.sort;

import com.sagansar.todo.model.worker.Worker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WorkerSkillSorterTest {

    @Test
    void sort_workers() {
        List<Worker> mockData = getMockWorkers();
        mockData = WorkerSkillSorter.sort(mockData, getMockSkills());
        assertTrue(mockData.get(0).getId() < mockData.get(1).getId());
        assertTrue(mockData.get(1).getId() < mockData.get(2).getId());
        assertTrue(mockData.get(2).getId() < mockData.get(3).getId());
    }

    private List<Worker> getMockWorkers() {
        return List.of(
                createMockWorker("HTML, CSS", 2),
                createMockWorker("", 4),
                createMockWorker("CSS, JavaScript, HTML,PHP", 1),
                createMockWorker("JavaScript, Python", 3)
        );
    }

    private String[] getMockSkills() {
        return new String[] {
                "HTML",
                "CSS",
                "JavaScript",
                "PHP"
        };
    }

    private Worker createMockWorker(String skills, int number) {
        Worker worker = new Worker();
        worker.setInfo(skills);
        worker.setId(number);
        return worker;
    }
}