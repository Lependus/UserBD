package org.example;

import org.example.event.UserEvent;
import org.example.event.UserOperation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository usersRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public UserService(UserRepository usersRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.usersRepository = usersRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional(readOnly = true)
    public Optional<User> findUser(long id) {
        return usersRepository.findById(id);
    }

    public User saveUser(User user) {
        User result = usersRepository.save(user);

        applicationEventPublisher.publishEvent(new UserEvent(UserOperation.CREATED, result.getEmail()));

        return result;
    }

    public boolean deleteUser(long id) {
        User user = usersRepository.findById(id).orElse(null);
        if (user == null){
            return false;
        }
        else {
            usersRepository.delete(user);
            applicationEventPublisher.publishEvent(new UserEvent(UserOperation.DELETED, user.getEmail()));
            return true;
        }
    }

    public boolean updateUser(long id, String name, String email, int age) {
        User user = usersRepository.findById(id).orElse(null);
        if(user == null){
            return false;
        }
        else {
            user.update(name, email, age);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return usersRepository.findAll();
    }

}
