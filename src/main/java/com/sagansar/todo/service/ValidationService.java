package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.external.TaskForm;
import com.sagansar.todo.model.external.WorkerProfileForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service
public class ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);

    private final RestOperations restOperations;

    public ValidationService() {
        restOperations = new RestTemplate();
    }

    public void validate(WorkerProfileForm profileForm) throws BadRequestException {

    }

    public void validate(TaskForm taskForm) throws BadRequestException {
        if (taskForm == null) {
            throw new BadRequestException("Форма создания задачи не может быть null!");
        }
    }

    private void validatePhone(String phone) {

    }

    public void validateVk(String vk) throws BadRequestException {
        try {
            ResponseEntity<String> response = restOperations.getForEntity("https://vk.com/" + vk, String.class);
            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                logger.error("Страницы с адресом vk.com/{} не существует, статус {}", vk, response.getStatusCode());
                throw new BadRequestException("Страницы с адресом vk.com/" + vk + " не существует");
            }
            String responseBody = response.getBody();
            if (!StringUtils.hasText(responseBody)) {
                logger.error("Страница с адресом vk.com/{}: ответ сервера пуст, статус {}", vk, response.getStatusCode());
                throw new BadRequestException("Что-то пошло не так! Покажите это администратору: https://vk.com/" + vk + " code " + response.getStatusCode());
            }
            if (responseBody.contains("service_msg_null\">Страница удалена")) {
                throw new BadRequestException("Страница с адресом vk.com/" + vk + " удалена");
            }
            if (responseBody.contains("service_msg_null\">К сожалению, нам")) {
                throw new BadRequestException("Страница с адресом vk.com/" + vk + " заблокирована");
            }
            if (!responseBody.contains("pp_last_activity")) {
                throw new BadRequestException("Страница с адресом vk.com/" + vk + " не принадлежит пользователю");
            }
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 404) {
                throw new BadRequestException("Страницы с адресом vk.com/" + vk + " не существует");
            } else {
                logger.error("Необычный ответ при попытке получить vk.com/{} : статус {}\n{}", vk, e.getRawStatusCode(), e.getMessage());
                throw new BadRequestException("Что-то пошло не так! Покажите этот код администратору: " + e.getRawStatusCode());
            }
        }
    }

    /**
     * Validate pagination from client
     *
     * @param pageable pagination
     * @param sort sorting parameter
     * @param direction sorting direction
     * @return pagination with sorting
     * @throws BadRequestException if there is no parameter or direction
     */
    public Pageable validatePageRequest(Pageable pageable, String sort, String direction) throws BadRequestException {
        Sort sortFilter = validateSort(sort, direction);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortFilter);
    }

    private Sort validateSort(String sort, String direction) throws BadRequestException {
        if (!StringUtils.hasText(sort)) {
            throw new BadRequestException("Не указан параметр для сортировки");
        }
        if (!StringUtils.hasText(direction)) {
            throw new BadRequestException("Не указано направление сортировки");
        }
        if ("desc".equalsIgnoreCase(direction)) {
            return Sort.by(Sort.Direction.DESC, sort);
        }
        return Sort.by(Sort.Direction.ASC, sort);
    }

}
