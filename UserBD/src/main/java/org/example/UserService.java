package org.example;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final SessionFactory factory;
    private UserDaoImpl usersDao = new UserDaoImpl(HibernateSessionFactoryUtil.getSessionFactory());

    public UserService(SessionFactory factory) {
        this.factory = factory;
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
