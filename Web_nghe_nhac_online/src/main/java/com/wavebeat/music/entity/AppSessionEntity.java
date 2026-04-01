package com.wavebeat.music.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_sessions")
public class AppSessionEntity {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_user_id")
    private AppUserEntity currentUser;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AppUserEntity getCurrentUser() { return currentUser; }
    public void setCurrentUser(AppUserEntity currentUser) { this.currentUser = currentUser; }
}
