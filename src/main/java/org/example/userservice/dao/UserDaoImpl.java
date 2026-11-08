package org.example.userservice.dao;


import org.example.userservice.config.HibernateUtil;
import org.example.userservice.entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.exception.ConstraintViolationException;
import org.example.userservice.exception.DuplicateEmailException;

public class UserDaoImpl implements UserDao {

    private static final Logger logger =
            LoggerFactory.getLogger(UserDaoImpl.class);


    @Override
    public void create(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
                session.persist(user);
                transaction.commit();

                logger.info("Пользователь с ID {} успешно создан", user.getId());

            } catch (RuntimeException e) {
                logger.error("Ошибка при создании пользователя", e);

                if (transaction.isActive()) {
                    try {
                        transaction.rollback();
                    } catch (RuntimeException rollbackException) {
                        e.addSuppressed(rollbackException);
                        logger.error("Ошибка отката транзакции", rollbackException);
                    }
                }

                if (e instanceof ConstraintViolationException cve
                        && "23505".equals(cve.getSQLState())) {
                    throw new DuplicateEmailException(
                            "Пользователь с таким email уже существует",
                            e
                    );
                }

                throw e;
            }
        }
    }


    @Override
    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            User user = session.find(User.class, id);

            return Optional.ofNullable(user);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            return session.createQuery("FROM User", User.class).getResultList();
        }
    }

    @Override
    public void update(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
                session.merge(user);
                transaction.commit();
                logger.info("Пользователь с ID {} успешно обновлен", user.getId());
            }
            catch (RuntimeException e) {
                logger.error("Ошибка при обновлении пользователя с ID {}", user.getId(), e);

                if (transaction.isActive()) {
                    try {
                        transaction.rollback();
                    } catch (RuntimeException rollbackException) {
                        e.addSuppressed(rollbackException);
                        logger.error("Ошибка отката транзакции", rollbackException);
                    }
                }

                if (e instanceof ConstraintViolationException cve
                        && "23505".equals(cve.getSQLState())) {
                    throw new DuplicateEmailException(
                            "Пользователь с таким email уже существует",
                            e
                    );
                }

                throw e;
            }


        }
    }

    @Override
    public void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
                User user = session.find(User.class, id);

                if (user != null) {
                    session.remove(user);
                }

                transaction.commit();

                if (user != null) {
                    logger.info("Пользователь с ID {} успешно удалён", id);
                } else {
                    logger.warn("Пользователь с ID {} не найден для удаления", id);
                }

            } catch (RuntimeException e) {
                logger.error("Ошибка при удалении пользователя", e);
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                throw e;
            }

        }
    }

}