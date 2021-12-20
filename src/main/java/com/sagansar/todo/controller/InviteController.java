package com.sagansar.todo.controller;

import com.sagansar.todo.infrastructure.exceptions.UnauthorizedException;
import com.sagansar.todo.model.work.Invite;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Transactional
@AllArgsConstructor
@RequestMapping("/invite")
public class InviteController {

    private final SecurityService securityService;

    @GetMapping("")
    public ModelAndView followTaskInvite(@RequestParam(name = "token") String token, ModelAndView modelAndView) {
        try {
            Invite invite = securityService.authorizeInviteToken(token);
            TodoTask task = invite.getTask();
            if (task == null) {
                return modelAndView;
            }
            modelAndView.addObject("inviteId", invite.getId());
            modelAndView.addObject("header", task.getHeader());
            modelAndView.addObject("message", task.getDescription() + " \nВыполнить до: " + task.getDeadline());
            modelAndView.addObject("skills", task.getStack());
            modelAndView.setViewName("invite-ok");
            return modelAndView;
        } catch (UnauthorizedException ex) {
            return modelAndView;
        }
    }
}
