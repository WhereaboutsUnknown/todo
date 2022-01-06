package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.FileBasic;
import com.sagansar.todo.model.work.TaskFile;

public class FileMapper {

    public static FileBasic fileToBasic(TaskFile file) {
        if (file == null) {
            return null;
        }
        FileBasic view = new FileBasic();
        view.setId(file.getId());
        view.setName(file.getName());
        view.setSize(file.getSize());
        view.setVideo(isVideo(file.getName()));
        return view;
    }

    public static boolean isVideo(String fileName) {
        return fileName.toUpperCase().endsWith(".MP4") || fileName.toUpperCase().endsWith(".AVI");
    }
}
