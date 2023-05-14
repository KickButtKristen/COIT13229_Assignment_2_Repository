package com.mycompany.assignment1.service;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

public abstract class AbstractFacade<T> {

    private Class<T> entityClass;

    public AbstractFacade(Class<T> entityClass) {
        if (entityClass == null) {
            throw new IllegalArgumentException("Entity class cannot be null.");
        }
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

    public void create(T entity) {
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("EntityManager has not been initialized.");
        }
        em.persist(entity);
    }

    public void edit(T entity) {
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("EntityManager has not been initialized.");
        }
        em.merge(entity);
    }

    public void remove(T entity) {
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("EntityManager has not been initialized.");
        }
        em.remove(em.merge(entity));
    }

    public T find(Object id) {
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("EntityManager has not been initialized.");
        }
        return em.find(entityClass, id);
    }

    public List<T> findAll() {
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("EntityManager has not been initialized.");
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();
        if (cb == null) {
            throw new IllegalStateException("CriteriaBuilder has not been initialized.");
        }
        CriteriaQuery cq = cb.createQuery();
        cq.select(cq.from(entityClass));
        return em.createQuery(cq).getResultList();
    }

    public List<T> findRange(int[] range) {
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("EntityManager has not been initialized.");
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();
        if (cb == null) {
            throw new IllegalStateException("CriteriaBuilder has not been initialized.");
        }
        CriteriaQuery cq = cb.createQuery();
        cq.select(cq.from(entityClass));
        javax.persistence.Query q = em.createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    public int count() {
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("EntityManager has not been initialized.");
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();
        if (cb == null) {
            throw new IllegalStateException("CriteriaBuilder has not been initialized.");
        }
        CriteriaQuery cq = cb.createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        cq.select(cb.count(rt));
        javax.persistence.Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
}
