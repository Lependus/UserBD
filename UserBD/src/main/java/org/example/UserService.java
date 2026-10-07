package org.example;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserDAO usersDao;

    public UserService(UserDAO userDAO) {
        this.usersDao = userDAO;
    }

    public Optional<User> findUser(long id) {
        return usersDao.findById(id);
    }

    public void saveUser(User user) {
        usersDao.save(user);
    }

    public boolean deleteUser(long id) {
        return  usersDao.delete(id);
    }

    public boolean updateUser(long id, String name, String email, int age) {
        return usersDao.update(id, name, email, age);
    }

    public List<User> findAllUsers() {
        return usersDao.findAll();
    }

}
