package org.example.userservice;

import org.example.userservice.config.HibernateUtil;
import org.example.userservice.console.ConsoleMenu;
import org.example.userservice.dao.UserDao;
import org.example.userservice.dao.UserDaoImpl;
import org.example.userservice.service.UserService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger =
            LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        boolean initialized = false;

        try {
            SessionFactory sessionFactory =
                    HibernateUtil.getSessionFactory();

            initialized = true;

            try (Session session = sessionFactory.openSession()) {
                session.createNativeQuery("SELECT 1", Integer.class)
                        .getSingleResult();
            }

            logger.info("Приложение запущено");

            UserDao userDao = new UserDaoImpl();
            UserService userService = new UserService(userDao);
            ConsoleMenu menu = new ConsoleMenu(userService);

            menu.run();

        } catch (ExceptionInInitializerError e) {
            logger.error("Ошибка инициализации Hibernate", e);
            System.out.println(
                    "Не удалось запустить Hibernate. Проверьте настройки и доступность PostgreSQL."
            );

        } catch (RuntimeException e) {
            logger.error("Ошибка работы приложения", e);
            System.out.println(
                    "Приложение завершено из-за ошибки. Подробности в логах."
            );

        } finally {
            if (initialized) {
                try {
                    HibernateUtil.shutdown();
                } catch (RuntimeException e) {
                    logger.error("Ошибка закрытия SessionFactory", e);
                }
            }
        }
    }
}