package com.inventory2.inventoryManagement2.repository;

import com.inventory2.inventoryManagement2.entity.Supplier;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SupplierRepository {

    private final SessionFactory sessionFactory;

    public Supplier save(Supplier supplier) {
        Session session = sessionFactory.getCurrentSession();
        if (supplier.getId() == null) {
            session.persist(supplier);
        } else {
            supplier = (Supplier) session.merge(supplier);
        }
        return supplier;
    }

    public Optional<Supplier> findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return Optional.ofNullable(session.get(Supplier.class, id));
    }

    public List<Supplier> findAll() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("from Supplier", Supplier.class).list();
    }

    public void delete(Supplier supplier) {
        Session session = sessionFactory.getCurrentSession();
        session.remove(supplier);
    }
}