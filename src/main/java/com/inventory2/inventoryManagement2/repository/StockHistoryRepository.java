package com.inventory2.inventoryManagement2.repository;

import com.inventory2.inventoryManagement2.entity.StockHistory;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockHistoryRepository {

    private final SessionFactory sessionFactory;

    // ✅ SAVE history
    public void save(StockHistory history) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(history);
    }

    // ✅ GET ALL history (SAFE - no timestamp usage)
    public List<StockHistory> findAll() {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery(
                "FROM StockHistory ORDER BY id DESC",
                StockHistory.class
        ).getResultList();
    }

    // ✅ GET BY PRODUCT ID (SAFE)
    public List<StockHistory> findByProductId(Long productId) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery(
                        "FROM StockHistory WHERE productId = :productId ORDER BY id DESC",
                        StockHistory.class
                )
                .setParameter("productId", productId)
                .getResultList();
    }
}