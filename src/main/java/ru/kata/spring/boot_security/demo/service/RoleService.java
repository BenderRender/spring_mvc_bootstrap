package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    List<Role> findAll();
    Role findById(int id);
    Optional<Role> findByName(String name);
    public void save(Role role);
    public void update(int id, Role updatedRole);
    public void delete(int id);

}