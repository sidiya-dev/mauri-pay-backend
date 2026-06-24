package com.mauripay.backend.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role = "USER";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    protected AppUser() {
    }

    public AppUser(String phone, String fullName, String passwordHash) {
        this.phone = phone;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // --- Brute-force protection -------------------------------------------------

    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    public long remainingLockSeconds() {
        if (lockedUntil == null) {
            return 0;
        }
        long secs = lockedUntil.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(secs, 0);
    }

    /**
     * Records a failed login. Once {@code maxAttempts} consecutive failures are reached the
     * account is locked for {@code lockMinutes} and the counter resets.
     */
    public void registerFailedAttempt(int maxAttempts, int lockMinutes) {
        failedAttempts++;
        if (failedAttempts >= maxAttempts) {
            lockedUntil = Instant.now().plusSeconds(lockMinutes * 60L);
            failedAttempts = 0;
        }
    }

    public void resetFailedAttempts() {
        failedAttempts = 0;
        lockedUntil = null;
    }
}
