package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.StatisticsBasic;
import com.sagansar.todo.controller.dto.StatisticsFull;
import com.sagansar.todo.model.work.Statistics;

public class StatisticsMapper {

    public static StatisticsBasic statisticsToBasic(Statistics statistics) {
        if (statistics == null) {
            return null;
        }
        StatisticsBasic dto = new StatisticsBasic();
        dto.setId(statistics.getId());
        int done = statistics.getDone();
        int failed = statistics.getFailed();
        dto.setDone(done);
        dto.setFailed(failed);
        int total = done + failed;
        double doneShare = calculateShare(done, total);
        dto.setDoneShare(doneShare);
        dto.setFailedShare(1.0 - doneShare);
        dto.setPoints(statistics.getPoints());
        return dto;
    }

    public static StatisticsFull statisticsToFull(Statistics statistics) {
        if (statistics == null) {
            return null;
        }
        StatisticsFull dto = new StatisticsFull();
        dto.setId(statistics.getId());
        int done = statistics.getDone();
        int failed = statistics.getFailed();
        int rejected = statistics.getRejected();
        int total = done + failed + rejected;
        double doneShare = calculateShare(done, total);
        double failedShare = calculateShare(failed, total);
        dto.setDone(done);
        dto.setFailed(failed);
        dto.setRejected(rejected);
        dto.setDoneShare(doneShare);
        dto.setFailedShare(failedShare);
        dto.setRejectedShare(1.0 - doneShare - failedShare);
        dto.setResponsible(statistics.getResponsible());
        dto.setPoints(statistics.getPoints());
        return dto;
    }

    private static double calculateShare(int part, int total) {
        double partDouble = part * 1.0;
        double totalDouble = total * 1.0;
        return partDouble / totalDouble;
    }
}
