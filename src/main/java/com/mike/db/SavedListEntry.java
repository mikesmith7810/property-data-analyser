package com.mike.db;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "saved_list_entries",
       uniqueConstraints = @UniqueConstraint(columnNames = {"list_id", "property_id"}))
public class SavedListEntry extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "list_id", nullable = false)
    public Long listId;

    @Column(name = "property_id", nullable = false)
    public Long propertyId;
}
