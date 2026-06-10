package com.inventory2.inventoryManagement2.repository;

import com.inventory2.inventoryManagement2.entity.Product;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductRepository {

    private final SessionFactory sessionFactory;

    public Product save(Product product) {
        Session session = sessionFactory.getCurrentSession();
        if (product.getId() == null) {
            session.persist(product);
        } else {
            product = (Product) session.merge(product);
        }
        return product;
    }

    public Optional<Product> findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return Optional.ofNullable(session.get(Product.class, id));
    }

    public List<Product> findAll() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("from Product", Product.class).list();
    }

    public void delete(Product product) {
        Session session = sessionFactory.getCurrentSession();
        session.remove(product);
    }
}