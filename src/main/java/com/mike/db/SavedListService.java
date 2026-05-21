package com.mike.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class SavedListService {

    @Inject
    EntityManager em;

    public List<SavedList> findAllLists() {
        return em.createQuery("SELECT l FROM SavedList l ORDER BY l.createdAt DESC", SavedList.class)
                .getResultList();
    }

    @Transactional
    public SavedList createList(String name) {
        SavedList list = new SavedList();
        list.name = name;
        em.persist(list);
        return list;
    }

    @Transactional
    public boolean deleteList(Long listId) {
        int entriesDeleted = em.createQuery(
                "DELETE FROM SavedListEntry e WHERE e.listId = :listId")
                .setParameter("listId", listId)
                .executeUpdate();
        int deleted = em.createQuery(
                "DELETE FROM SavedList l WHERE l.id = :listId")
                .setParameter("listId", listId)
                .executeUpdate();
        return deleted > 0;
    }

    public List<SavedListEntry> findEntriesForList(Long listId) {
        return em.createQuery(
                "SELECT e FROM SavedListEntry e WHERE e.listId = :listId", SavedListEntry.class)
                .setParameter("listId", listId)
                .getResultList();
    }

    public Set<Long> findPropertyIdsInList(Long listId) {
        return em.createQuery(
                "SELECT e.propertyId FROM SavedListEntry e WHERE e.listId = :listId", Long.class)
                .setParameter("listId", listId)
                .getResultStream()
                .collect(Collectors.toSet());
    }

    @Transactional
    public boolean addEntry(Long listId, Long propertyId) {
        try {
            SavedListEntry entry = new SavedListEntry();
            entry.listId = listId;
            entry.propertyId = propertyId;
            em.persist(entry);
            em.flush();
            return true;
        } catch (PersistenceException e) {
            return false;
        }
    }

    @Transactional
    public boolean removeEntry(Long listId, Long propertyId) {
        int deleted = em.createQuery(
                "DELETE FROM SavedListEntry e WHERE e.listId = :listId AND e.propertyId = :propertyId")
                .setParameter("listId", listId)
                .setParameter("propertyId", propertyId)
                .executeUpdate();
        return deleted > 0;
    }

    public long countEntriesForList(Long listId) {
        return em.createQuery(
                "SELECT COUNT(e) FROM SavedListEntry e WHERE e.listId = :listId", Long.class)
                .setParameter("listId", listId)
                .getSingleResult();
    }
}
