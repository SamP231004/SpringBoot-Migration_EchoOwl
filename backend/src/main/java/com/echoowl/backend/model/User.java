package com.echoowl.backend.model;

import com.echoowl.backend.util.Cuid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "`User`")
public class User {
    @Id
    @Column(name = "`id`", nullable = false)
    private String id;

    @Column(name = "`externalId`", unique = true)
    private String externalId;

    @Column(name = "`quotaLimit`", nullable = false)
    private int quotaLimit;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "`plan`", nullable = false, columnDefinition = "`Plan`")
    private Plan plan = Plan.FREE;

    @Column(name = "`email`", nullable = false, unique = true)
    private String email;

    @Column(name = "`apiKey`", nullable = false, unique = true)
    private String apiKey;

    @Column(name = "`discordId`")
    private String discordId;

    @CreationTimestamp
    @Column(name = "`createdAt`", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "`updatedAt`", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = Cuid.create();
        }
        if (apiKey == null) {
            apiKey = Cuid.create();
        }
    }

    public String getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public int getQuotaLimit() {
        return quotaLimit;
    }

    public void setQuotaLimit(int quotaLimit) {
        this.quotaLimit = quotaLimit;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
