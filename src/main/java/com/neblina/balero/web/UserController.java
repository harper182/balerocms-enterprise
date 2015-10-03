/**
 * Balero CMS Project: Proyecto 100% Mexicano de código libre.
 * Página Oficial: http://www.balerocms.com
 *
 * @author      Anibal Gomez <anibalgomez@icloud.com>
 * @copyright   Copyright (C) 2015 Neblina Software. Derechos reservados.
 * @license     Licencia BSD; vea LICENSE.txt
 */

package com.neblina.balero.web;

import com.neblina.balero.domain.User;
import com.neblina.balero.service.UserService;
import com.neblina.balero.service.repository.PageRepository;
import com.neblina.balero.service.repository.PropertyRepository;
import com.neblina.balero.service.repository.SettingRepository;
import com.neblina.balero.service.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Locale;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LogManager.getLogger(UserController.class.getName());

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String registerForm(Model model,
                               Locale locale,
                               @ModelAttribute(value="user") @Valid User user, BindingResult bindingResultUser) {
        if(bindingResultUser.hasErrors()) {
            //model.addAttribute("user", user);
        }
        String lang = locale.getLanguage();
        model.addAttribute("settings", settingRepository.findOneByCode(locale.getLanguage()));
        model.addAttribute("pages", pageRepository.findAllByCode(lang));
        model.addAttribute("properties", propertyRepository.findOneById(1L));
        return "silbato/register";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(Model model,
                           Locale locale,
                           @RequestParam(value = "username") String username,
                           @RequestParam(value = "password") String password,
                           @RequestParam(value = "passwordVerify") String passwordVerify,
                           @RequestParam(value = "firstName") String firstName,
                           @RequestParam(value = "lastName") String lastName,
                           @RequestParam(value = "email") String email,
                           @ModelAttribute(value="user") @Valid User user, BindingResult bindingResultUser) {
        log.debug("Creating user... " + username);
        if(!password.equals(passwordVerify)) {
            bindingResultUser.rejectValue("passwordVerify", "error.passwordVerify", "Do not match.");
        }
        if(username.contains("admin")) {
            bindingResultUser.rejectValue("username", "error.username", "You can't use this username.");
        }
        if(bindingResultUser.hasErrors()) {
            model.addAttribute("settings", settingRepository.findOneByCode(locale.getLanguage()));
            return "silbato/register";
        }
        User usr = userRepository.findOneByUsername(username);
        if(usr != null) {
            log.debug("Username value: " + usr.getUsername());
        }
        if(usr == null) {
            log.debug("Username NOT found");
            User usr2 = userRepository.findOneByEmail(email);
            if(usr2 != null) { // email found
                log.debug("Email already exists. Register with this email.");
                userService.deleteUserEmail(usr2.getId()); // Clean
                userService.createUserAccount(username, password, passwordVerify, firstName, lastName, usr2.getEmail(), true, "ROLE_USER", "user"); // Add
            } else {
                userService.createUserAccount(username, password, passwordVerify, firstName, lastName, email, true, "ROLE_USER", "user");
            }
        }
        return "redirect:/login";
    }

}
