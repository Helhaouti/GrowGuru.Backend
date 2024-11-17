package nl.growguru.app.models.auth;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.growguru.app.models.plantspaces.Space;
import nl.growguru.app.models.shop.PurchaseRecord;
import nl.growguru.app.views.GrowGuruViews;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static nl.growguru.app.config.GenericConfig.EMAIL_REGEX;
import static nl.growguru.app.config.GenericConfig.PASSWORD_REGEX;

/**
 * Represents a user entity in the GrowGuru application.
 * This class encapsulates user details and authentication-related information.
 */
@Entity

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        scope = GrowGuru.class,
        property = "id"
)
public class GrowGuru implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonView(GrowGuruViews.PurchaseRecordFilter.class)
    private UUID id;

    @JsonView({GrowGuruViews.SpaceFilter.class, GrowGuruViews.SpaceMemberFilter.class, GrowGuruViews.UserViewFilter.class})
    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    @JsonView(GrowGuruViews.UserViewFilter.class)
    private byte[] picture;

    @JsonIgnore
    private String password;

    @JsonIgnore
    @Column(unique = true)
    private String verificationCode;

    @CreationTimestamp
    @Setter(value = AccessLevel.NONE)
    @JsonView(GrowGuruViews.UserViewFilter.class)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Setter(value = AccessLevel.NONE)
    private LocalDateTime changedAt;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "guru_spaces",
            joinColumns = @JoinColumn(name = "guru_id"),
            inverseJoinColumns = @JoinColumn(name = "space_id")
    )
    @JsonView(GrowGuruViews.UserViewFilter.class)
    private Set<Space> spaces = new HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "guru_admin_spaces",
            joinColumns = @JoinColumn(name = "admin_guru_id"),
            inverseJoinColumns = @JoinColumn(name = "admin_space_id")
    )
    @JsonView(GrowGuruViews.UserViewFilter.class)
    private Set<Space> adminSpaces = new HashSet<>();

    @Builder.Default
    private long currency = 0;

    @Builder.Default
    @JsonView(GrowGuruViews.UserViewFilter.class)
    private long experience = 0;

    private boolean enabled = false;

    @Builder.Default
    @OneToMany(mappedBy = "buyer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<PurchaseRecord> purchases = new ArrayList<>();

    @Builder.Default
    @JsonView(GrowGuruViews.UserViewFilter.class)
    private GrowGurePremium premium = GrowGurePremium.NONE;

    @JsonIgnore
    public Set<Space> getAllSpaces() {
        return Stream.concat(spaces.stream(), adminSpaces.stream()).collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GrowGuru growGuru)) return false;

        return getId().equals(growGuru.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * DTO for user login data, encapsulating username and password.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginDto {
        @NotBlank(message = "Not provided.")
        String username;

        @NotBlank(message = "Not provided.")
        @Pattern(message = "Is invalid.", regexp = PASSWORD_REGEX)
        String password;

        public UsernamePasswordAuthenticationToken toAuthToken() {
            return new UsernamePasswordAuthenticationToken(this.getUsername(), this.getPassword());
        }
    }

    /**
     * DTO for registering a new user, extending from LoginDto and including an email field.
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class RegisterDto extends LoginDto {

        @NotBlank(message = "Not provided.")
        @Email(message = "Is invalid.", regexp = EMAIL_REGEX)
        private String email;

        public RegisterDto(String username, String password, String email) {
            super(username, password);
            this.email = email;
        }

        public GrowGuru toGrowGuru(PasswordEncoder encoder, String verificationCode) {
            return builder()
                    .email(getEmail())
                    .password(encoder.encode(getPassword()))
                    .username(getUsername())
                    .verificationCode(verificationCode)
                    .build();
        }
    }

    /**
     * DTO for editing an existing user.
     */
    @Data
    public static class UpdateDto {
        @Email(regexp = EMAIL_REGEX, message = "Is invalid.")
        private String email;
        @Pattern(regexp = PASSWORD_REGEX, message = "Is invalid.")
        private String password;
        private String username;
        private byte[] picture;
    }
}