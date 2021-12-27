package com.sagansar.todo.infrastructure.sort;

import com.sagansar.todo.model.worker.Worker;

import java.util.ArrayList;
import java.util.List;

public class WorkerSkillSorter {

    public static List<Worker> sort(List<Worker> workers, String[] skills) {
        List<Worker> newList = new ArrayList<>(workers);
        newList.sort((a, b) -> compareBySkills(a, b, skills));
        return newList;
    }

    private static int compareBySkills(Worker first, Worker second, String[] skills) {
        int firstPoints = 0;
        int secondPoints = 0;
        String firstSkills = first.getInfo();
        String secondSkills = second.getInfo();
        if (secondSkills == null || firstSkills == null) {
            if (secondSkills != null) {
                return 1;
            }
            if (firstSkills != null) {
                return -1;
            }
            return 0;
        }
        for (String skill : skills) {
            if (firstSkills.contains(skill)) {
                firstPoints++;
            }
            if (secondSkills.contains(skill)) {
                secondPoints++;
            }
        }
        return Integer.compare(secondPoints, firstPoints);
    }
}
