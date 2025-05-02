package com.example.epic.security;

import com.example.epic.user.SiteUser;
import jakarta.persistence.*;
        import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(schema="epic", name="refresh_token")
public class RefreshToken {
    @Id
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName="id")
    private SiteUser user;
    private String refreshToken;

    public RefreshToken(SiteUser user, String refreshToken) {
        this.user = user;
        this.refreshToken = refreshToken;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
