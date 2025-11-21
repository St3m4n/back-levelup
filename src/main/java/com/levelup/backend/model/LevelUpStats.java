package com.levelup.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "levelup_stats")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelUpStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 32)
    private String run;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false, name = "exp_compras")
    private int expCompras;

    @Column(nullable = false, name = "exp_torneos")
    private int expTorneos;

    @Column(nullable = false, name = "exp_referidos")
    private int expReferidos;

    @Column(nullable = false, unique = true, length = 16)
    private String referralCode;

    private String referredBy;

    @OneToMany(mappedBy = "stats", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Referral> referrals = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
