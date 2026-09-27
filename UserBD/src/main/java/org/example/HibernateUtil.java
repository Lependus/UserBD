package org.example;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private HibernateUtil() {}

    public static SessionFactory createSessionFactory() {
        return new Configuration()
                .configure()
                .addAnnotatedClass(User.class)
                .buildSessionFactory();
    }
}
