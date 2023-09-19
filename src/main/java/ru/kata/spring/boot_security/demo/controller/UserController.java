package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;


@Controller
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/index")
    public String showIndex() {
        return "index";
    }

    @GetMapping("/admin")
    public String showAllUsers(Model model) {
        model.addAttribute("users", userService.findAll());

        return "users/all_users";
    }

    @GetMapping("/user")
    public String showUserDetails(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByName(username);
        if (user == null) {
            return "access-denied";
        }
        model.addAttribute("user", user);

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_USER"))) {
            model.addAttribute("hasUserRole", true);
        }
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            model.addAttribute("hasAdminRole", true);
        }

        return "user";
    }

    @GetMapping("/{id}")
    public String showUserDetails(Model model, @PathVariable("id") int id, Principal principal) {
        User user = userService.findById(id);

        if (user == null) {
            return "access-denied";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));


        if (!isAdmin) {
            String username = principal.getName();
            if (!user.getName().equals(username)) {
                return "access-denied";
            }
        }

        model.addAttribute("user", user);
        return "user";
    }

    @GetMapping("/new")
    public String newPerson(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.findAll());
        return "users/new";
    }

    @PostMapping()
    public String create(@ModelAttribute("user") @Valid User user,
                         BindingResult bindingResult,
                         @RequestParam(value = "selectedRoles", required = false) List<Integer> selectedRoles) {
        if (bindingResult.hasErrors()) {
            return "users/new";
        }

        if (selectedRoles != null) {
            Set<Role> roles = selectedRoles.stream()
                    .map(roleService::findById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        userService.save(user);

        return "redirect:/admin";
    }

    @GetMapping("/{id}/edit")
    public String edit(Model model, @PathVariable("id") int id) {
        User user = userService.findById(id);
        if (user == null) {
            return "access-denied";
        }

        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleService.findAll());

        return "users/edit";
    }

    @PatchMapping("/{id}")
    public String updateUser(@PathVariable("id") int userId, @ModelAttribute("user") User updatedUser) {
        User existingUser = userService.findById(userId);
        existingUser.setName(updatedUser.getName());
        existingUser.setSurname(updatedUser.getSurname());
        existingUser.setAge(updatedUser.getAge());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setRoles(updatedUser.getRoles());
        String newPassword = updatedUser.getPassword();

        if (newPassword != null && !newPassword.isEmpty() && !passwordEncoder.matches(newPassword, existingUser.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }

        userService.update(userId,existingUser);
        return "redirect:/admin";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") int id) {
        userService.delete(id);
        return "redirect:/admin";
    }
}
