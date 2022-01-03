package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.UnitBasic;
import com.sagansar.todo.controller.dto.UnitFull;
import com.sagansar.todo.model.manager.Unit;

import java.util.stream.Collectors;

public class UnitMapper {

    public static UnitBasic unitToBasic(Unit unit) {
        if (unit == null) {
            return null;
        }
        UnitBasic view = new UnitBasic();
        view.setId(unit.getId());
        view.setName(unit.getName());
        return view;
    }

    public static UnitFull unitToFull(Unit unit) {
        if (unit == null) {
            return null;
        }
        UnitFull view = new UnitFull();
        view.setId(unit.getId());
        view.setName(unit.getName());
        view.setDescription(unit.getDescription());
        view.setManagers(unit.getManagers().stream()
                .map(PersonMapper::managerToName)
                .collect(Collectors.toList()));
        return view;
    }
}
