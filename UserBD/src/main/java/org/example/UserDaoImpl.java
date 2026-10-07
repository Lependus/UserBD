package org.example;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class UserDaoImpl implements UserDAO{

    private final SessionFactory factory;

    public UserDaoImpl(SessionFactory factory) {
        this.factory = factory;
    }

    private <T> T execute (Function<Session, T> operation){
        try(Session session = factory.openSession()){
            Transaction tr = session.beginTransaction();

            try{
                T result = operation.apply(session);
                tr.commit();
                return result;
            } catch (RuntimeException e) {
                if (tr.isActive()){
                    tr.rollback();
                }
                throw e;
            }
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        return execute(session -> Optional.ofNullable(session.find(User.class, id)));
    }

    @Override
    public void save(User user) {
        execute(session -> {
            session.persist(user);
            return null;
        });
        System.out.println("Создан пользователь id=" + user.getId());
    }

    @Override
    public boolean update(long id, String name, String email, int age) {
        boolean isupdate = execute(session -> {
            User user = session.find(User.class, id);
            if (user == null){
                System.out.println("[INFO] Не найден пользователь с таким id=" + id);
                return  false;
            }

            user.update(name, email, age);
            return true;
        });

        if(isupdate){
            System.out.println("Обновлен пользователь id=" + id);
        }
        return isupdate;
    }

    @Override
    public boolean delete(long id) {
        boolean isdelete = execute(session -> {
            User user = session.find(User.class, id);
            if (user == null){
                System.out.println("[INFO] Не найден пользователь с таким id=" + id);
                return false;
            }

            session.remove(user);
            return true;
        });

        if(isdelete){
            System.out.println("Удалён пользователь id=" + id);
        }
        return isdelete;
    }

    @Override
    public List<User> findAll() {
        return execute(session -> session.createQuery("From User").list());
    }
}
