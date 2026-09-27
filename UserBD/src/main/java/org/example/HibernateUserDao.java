package org.example;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class HibernateUserDao implements UserDao {

    private final SessionFactory factory;

    public HibernateUserDao(SessionFactory factory) {
        this.factory = factory;
    }

    private <T> T execute(Function<Session, T> operation) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();

            try {
                T result = operation.apply(session);
                tx.commit();
                return result;
            } catch (RuntimeException e) {
                try {
                    if (tx.isActive()) {
                        tx.rollback();
                    }
                } catch (RuntimeException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException e) {
            System.out.println("[INFO] ООшибка операции с БД");
            throw new DaoException(e);
        }
    }

    @Override
    public void create(User user) {
        execute(session -> {
            session.persist(user);
            return null;
        });
        System.out.println("[INFO] Создан пользователь id=" + user.getId());
    }

    @Override
    public Optional<User> findById(long id) {
        return execute(session ->
                Optional.ofNullable(session.find(User.class, id)));
    }

    @Override
    public List<User> findAll() {
        return execute(session ->
                session.createQuery("from User order by id", User.class)
                        .getResultList());
    }

    @Override
    public boolean update(long id, String name, String email, int age) {
        boolean updated = execute(session -> {
            User user = session.find(User.class, id);
            if (user == null) {
                return false;
            }

            user.update(name, email, age);
            return true;
        });

        if (updated) {
            System.out.println("[INFO] Обновлён пользователь id=" + id);
        }
        return updated;
    }

    @Override
    public boolean delete(long id) {
        boolean deleted = execute(session -> {
            User user = session.find(User.class, id);
            if (user == null) {
                return false;
            }

            session.remove(user);
            return true;
        });

        if (deleted) {
            System.out.println("[INFO] Удалён пользователь id=" + id);
        }
        return deleted;
    }
}