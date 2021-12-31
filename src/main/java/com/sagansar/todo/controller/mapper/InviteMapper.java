package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.InviteDto;
import com.sagansar.todo.model.work.Invite;

public class InviteMapper {

    public static InviteDto inviteToDto(Invite invite) {
        InviteDto dto = new InviteDto();
        dto.setTaskId(invite.getTask().getId());
        dto.setWorkerId(invite.getWorker().getId());
        dto.setChecked(invite.isChecked());
        dto.setAccepted(invite.isAccepted());
        dto.setCreationTime(invite.getCreationTime());
        return dto;
    }
}
