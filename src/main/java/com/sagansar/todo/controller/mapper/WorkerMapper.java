package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.WorkerDto;
import com.sagansar.todo.controller.dto.WorkerFullDto;
import com.sagansar.todo.model.external.WorkerProfileForm;
import com.sagansar.todo.model.general.Contacts;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerMapper {

    private static final Logger logger = LoggerFactory.getLogger(WorkerMapper.class);

    public static WorkerDto workerToDto(Worker worker) {
        if (worker == null) {
            return null;
        }
        WorkerDto dto = new WorkerDto();
        dto.setId(worker.getId());
        dto.setName(worker.getName());
        dto.setAge(formatAge(worker.getAge()));
        dto.setSkills(worker.getInfo());
        User user = worker.getUser();
        if (user != null) {
            dto.setContacts(ContactsMapper.contactsToDto(user.getContacts()));
        }
        return dto;
    }

    public static WorkerFullDto workerToFullDto(Worker worker) {
        if (worker == null) {
            return null;
        }
        WorkerFullDto dto = new WorkerFullDto();
        dto.setId(worker.getId());
        dto.setName(worker.getName());
        dto.setAge(formatAge(worker.getAge()));
        dto.setSkills(worker.getInfo());
        User user = worker.getUser();
        if (user != null) {
            dto.setBirthDate(user.getBirthDate());
            dto.setFirstName(user.getFirstName());
            dto.setPatronym(user.getPatronym());
            dto.setSurname(user.getSurname());
            dto.setContacts(ContactsMapper.contactsToDto(user.getContacts()));
        }
        return dto;
    }

    public static Worker fromWorkerProfileForm(WorkerProfileForm form) {
        if (form == null) {
            return null;
        }
        Worker worker = new Worker();
        User user = new User();
        Contacts contacts = new Contacts();

        worker.setName(form.getProfileName());
        worker.setInfo(form.getSkills());

        user.setFirstName(form.getFirstName());
        user.setPatronym(form.getPatronym());
        user.setSurname(form.getSurname());

        contacts.setEmail(form.getEmail());
        contacts.setVk(form.getVk());
        contacts.setPhoneNumber(form.getPhoneNumber());
        contacts.setFacebook(form.getFacebook());
        contacts.setTelegram(form.getTelegram());
        contacts.setOther(form.getOther());
        user.setContacts(contacts);

        if (form.getBirthDate() != null) {
            user.setBirthDate(form.getBirthDate());
        }

        worker.setUser(user);
        return worker;
    }

    private static String formatAge(Integer age) {
        if (age == null) {
            return "";
        }
        if (age <= 20) {
            return age + " лет";
        }
        if (age % 10 == 1) {
            return age + " год";
        }
        if (age % 10 < 5) {
            return age + " года";
        }
        return age + " лет";
    }
}
