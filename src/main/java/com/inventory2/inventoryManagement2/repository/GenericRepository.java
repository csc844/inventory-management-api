package com.inventory2.inventoryManagement2.repository;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenericRepository {

    private final SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @SuppressWarnings("unchecked")
    public <T> T save(T entity) {

        return (T) getSession().merge(entity);
    }

    public <T, ID> Optional<T> findById(Class<T> clazz, ID id) {

        return Optional.ofNullable(
                getSession().get(clazz, id)
        );
    }

    public <T> List<T> findAll(Class<T> clazz) {

        return getSession()
                .createQuery(
                        "FROM " + clazz.getSimpleName(),
                        clazz
                )
                .getResultList();
    }

    public <T> void delete(T entity) {

        getSession().remove(entity);
    }

    public <T> List<T> findByProperty(
            Class<T> clazz,
            String hql,
            String parameterName,
            Object parameterValue) {

        return getSession()
                .createQuery(hql, clazz)
                .setParameter(parameterName, parameterValue)
                .getResultList();
    }
}