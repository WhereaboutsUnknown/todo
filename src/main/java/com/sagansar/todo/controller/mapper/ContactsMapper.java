package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.ContactsDto;
import com.sagansar.todo.model.general.Contacts;

public class ContactsMapper {

    public static ContactsDto contactsToDto(Contacts contacts) {
        if (contacts == null) {
            return null;
        }
        ContactsDto dto = new ContactsDto();
        dto.setPhoneNumber(contacts.getPhoneNumber());
        dto.setEmail(contacts.getEmail());
        dto.setVk(contacts.getVk());
        dto.setTelegram(contacts.getTelegram());
        dto.setFacebook(contacts.getFacebook());
        dto.setOther(contacts.getOther());
        return dto;
    }
}
