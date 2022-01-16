package com.sagansar.todo.infrastructure.sort;

import com.sagansar.todo.model.work.Statistics;
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
            return compareByStatistics(first, second);
        }
        for (String skill : skills) {
            if (firstSkills.contains(skill)) {
                firstPoints++;
            }
            if (secondSkills.contains(skill)) {
                secondPoints++;
            }
        }
        if (firstPoints == 0 && secondPoints == 0) {
            return compareByStatistics(first, second);
        }
        return Integer.compare(secondPoints, firstPoints);
    }

    private static int compareByStatistics(Worker first, Worker second) {
        Statistics firstStat = first.getStatistics();
        Statistics secondStat = second.getStatistics();
        if (firstStat == null || secondStat == null) {
            if (secondStat != null) {
                return 1;
            }
            if (firstStat != null) {
                return -1;
            }
        } else {
            Double pointsFirst = firstStat.getPoints();
            Double pointsSecond = secondStat.getPoints();
            if (pointsFirst == 0 && pointsSecond == 0) {
                return Integer.compare(secondStat.getDone() - secondStat.getFailed(), firstStat.getDone() - firstStat.getFailed());
            }
            return Double.compare(pointsSecond, pointsFirst);
        }
        return 0;
    }
}
