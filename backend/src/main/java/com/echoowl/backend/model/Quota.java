package com.echoowl.backend.model;

import com.echoowl.backend.util.Cuid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "`Quota`")
public class Quota {
    @Id
    @Column(name = "`id`", nullable = false)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`userId`", nullable = false, unique = true)
    private User user;

    @Column(name = "`year`", nullable = false)
    private int year;

    @Column(name = "`month`", nullable = false)
    private int month;

    @Column(name = "`count`", nullable = false)
    private int count;

    @UpdateTimestamp
    @Column(name = "`updatedAt`", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = Cuid.create();
        }
    }

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
