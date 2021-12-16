package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.UnitBasic;
import com.sagansar.todo.model.manager.Unit;

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
}
