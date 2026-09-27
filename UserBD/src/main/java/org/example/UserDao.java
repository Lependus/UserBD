package org.example;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    void create(User user);
    Optional<User> findById(long id);
    List<User> findAll();
    boolean update(long id, String name, String email, int age);
    boolean delete(long id);
}