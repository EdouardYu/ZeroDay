package com.software.security.zeroday.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "validation")
public class Validation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Column(name = "expired_at")
    private Instant expiredAt;

    private Boolean enabled;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.enabled);
    }
}
