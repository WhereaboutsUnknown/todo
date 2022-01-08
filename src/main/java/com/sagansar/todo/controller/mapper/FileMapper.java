package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.FileBasic;
import com.sagansar.todo.model.general.AbstractFile;
import com.sagansar.todo.model.general.Extension;

public class FileMapper {

    public static FileBasic fileToBasic(AbstractFile file) {
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
        return Extension.resolve(fileName).isVideo();
    }
}
