package com.software.security.zeroday.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.software.security.zeroday.entity.enumeration.FileExtension;
import com.software.security.zeroday.entity.enumeration.FileType;
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
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private FileExtension extension;

    @Column(name = "size_in_bytes")
    private Long size;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    private Boolean processed;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToOne
    @JoinColumn(name = "post_id")
    @JsonIgnore
    private Post post;

    @Transient
    private String type;

    public FileType getType() {
        return switch (this.extension) {
            case JPEG, JPG, PNG, GIF, WEBP, SVG, HEIC -> FileType.IMAGE;
            case MP4, WEBM, MOV -> FileType.VIDEO;
        };
    }

    public boolean isProcessed() {
        return Boolean.TRUE.equals(this.processed);
    }
}
