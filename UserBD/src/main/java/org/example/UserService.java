package org.example;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository usersRepository;

    public UserService(UserRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> findUser(long id) {
        return usersRepository.findById(id);
    }

    public User saveUser(User user) {
        return usersRepository.save(user);
    }

    public boolean deleteUser(long id) {
        User user = usersRepository.findById(id).orElse(null);
        if (user == null){
            return false;
        }
        else {
            usersRepository.delete(user);
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
