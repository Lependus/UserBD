package org.example;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findById(Long id);
    void save(User user);
    boolean update(long id, String name, String email, int age);
    boolean delete(long id);
    List<User> findAll();
}
