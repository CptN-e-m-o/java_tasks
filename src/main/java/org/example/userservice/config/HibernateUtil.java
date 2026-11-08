package org.example.userservice.config;

import org.example.userservice.entity.User;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {

    private static final SessionFactory sessionFactory =
            new Configuration()
                    .configure()
                    .addAnnotatedClass(User.class)
                    .buildSessionFactory();

    private HibernateUtil() {

    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        sessionFactory.close();
    }
}