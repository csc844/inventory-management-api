package com.inventory2.inventoryManagement2.repository;

import com.inventory2.inventoryManagement2.entity.Stock;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StockRepository {

    private final SessionFactory sessionFactory;

    public Stock save(Stock stock) {
        Session session = sessionFactory.getCurrentSession();
        if (stock.getId() == null) {
            session.persist(stock);
        } else {
            stock = (Stock) session.merge(stock);
        }
        return stock;
    }

    public Optional<Stock> findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return Optional.ofNullable(session.get(Stock.class, id));
    }

    public Optional<Stock> findByProductId(Long productId) {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("from Stock s where s.product.id = :productId", Stock.class)
                .setParameter("productId", productId)
                .uniqueResultOptional();
    }

    public List<Stock> findAll() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("from Stock", Stock.class).list();
    }

    public void delete(Stock stock) {
        Session session = sessionFactory.getCurrentSession();
        session.remove(stock);
    }
}