package ru.kata.spring.boot_security.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repositories.UserRepositories;
import ru.kata.spring.boot_security.demo.security.MyUserDetails;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MyUserDetailsService implements UserDetailsService,UserService {

    private final UserRepositories userRepositories;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MyUserDetailsService(UserRepositories userRepositories, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepositories = userRepositories;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        Optional<User> user = userRepositories.findByName(name);

        if (user.isEmpty())
            throw new UsernameNotFoundException("User not found");

        return new MyUserDetails(user.get());
    }
    @Override
    public User findByName(String name) {
        Optional<User> user = userRepositories.findByName(name);
        return user.orElse(null);
    }

    @Override
    public List<User> findAll(){
        return userRepositories.findAll();
    }

    @Override
    public User findById(int id) {
        Optional<User> foundUser = userRepositories.findById(id);
        return foundUser.orElse(null);
    }

    @Override
    @Transactional
    public void save(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepositories.save(user);
    }
    @Override
    @Transactional
    public void update(int id, User updateUser) {
        User existingUser = userRepositories.findById(id).orElse(null);
        if (existingUser != null) {
            updateUser.setId(id);
            updateUser.setPassword(existingUser.getPassword());
            userRepositories.save(updateUser);
        }
    }
    @Override
    @Transactional
    public void delete(int id) {
        Optional<User> userOptional = userRepositories.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.getRoles().clear();
            userRepositories.delete(user);
        }
    }

}
