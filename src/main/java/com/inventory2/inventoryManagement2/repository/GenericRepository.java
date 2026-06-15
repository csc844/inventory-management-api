package com.inventory2.inventoryManagement2.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenericRepository {

    private final SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @SuppressWarnings("unchecked")
    public <T> T save(T entity) {
        try {
            return (T) getSession().merge(entity);

        } catch (Exception e) {
            log.error("Failed to save entity: {}", entity, e);
            throw e;
        }
    }

    public <T, ID> Optional<T> findById(Class<T> clazz, ID id) {
        try {
            return Optional.ofNullable(
                    getSession().get(clazz, id)
            );

        } catch (Exception e) {
            log.error("Failed to find {} with id {}",
                    clazz.getSimpleName(), id, e);
            throw e;
        }
    }

    public <T> List<T> findAll(Class<T> clazz) {
        try {
            return getSession()
                    .createQuery(
                            "FROM " + clazz.getSimpleName(),
                            clazz
                    )
                    .getResultList();

        } catch (Exception e) {
            log.error("Failed to fetch all {}",
                    clazz.getSimpleName(), e);
            throw e;
        }
    }

    public <T> void delete(T entity) {
        try {
            getSession().remove(entity);

        } catch (Exception e) {
            log.error("Failed to delete entity: {}", entity, e);
            throw e;
        }
    }

    public <T> List<T> findByProperty(
            Class<T> clazz,
            String hql,
            String parameterName,
            Object parameterValue) {

        try {
            return getSession()
                    .createQuery(hql, clazz)
                    .setParameter(parameterName, parameterValue)
                    .getResultList();

        } catch (Exception e) {
            log.error(
                    "Failed query on {} using {}={}",
                    clazz.getSimpleName(),
                    parameterName,
                    parameterValue,
                    e
            );
            throw e;
        }
    }
}