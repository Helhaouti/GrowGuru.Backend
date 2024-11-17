package nl.growguru.app.services;

import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import nl.growguru.app.exceptions.ConflictException;
import nl.growguru.app.exceptions.ResourceNotFound;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.models.plantspaces.Space;
import nl.growguru.app.repositories.GrowGuruRepository;
import nl.growguru.app.repositories.ProductRepository;
import nl.growguru.app.repositories.SpaceRepository;
import nl.growguru.app.repositories.StripeSessionTrackerRepository;
import nl.growguru.app.utils.SecurityContextUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static org.springframework.util.StringUtils.hasLength;
import static org.springframework.util.StringUtils.hasText;

@Service
@Transactional
@RequiredArgsConstructor
public class GrowGuruService implements UserDetailsService {

    private final GrowGuruRepository growGuruRepo;
    private final PasswordEncoder encoder;
    private final ProductRepository productRepository;
    private final StripeSessionTrackerRepository trackerRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceService spaceService;
    private final ProductService productService;
    private final MailService mailService;

    /**
     * Returns a user entity, with the provided userName if it exists, otherwise null.
     */
    public GrowGuru findByUserName(String username) {
        return hasLength(username) ? growGuruRepo.findByUsername(username).orElse(null) : null;
    }

    public GrowGuru findByEmail(String email) {
        return growGuruRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFound(GrowGuru.class.getSimpleName(), "email", email));
    }

    public GrowGuru findById(UUID id) {
        return growGuruRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFound(GrowGuru.class.getSimpleName(), "id", id.toString()));
    }

    public List<GrowGuru> findAllInSpace(Space space) {
        List<GrowGuru> gurus = growGuruRepo.findAllMembersInSpace(space);

        if (gurus.isEmpty())
            throw new ResourceNotFound(GrowGuru.class.getName(), "space", space.toString());

        return gurus;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return growGuruRepo
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User with username - %s, not found", username)
                ));
    }

    /**
     * Creates a user based on the provided request.
     */
    public GrowGuru create(GrowGuru.RegisterDto req) {
        this.growGuruRepo.findByUsername(req.getUsername()).ifPresent(g -> {
            throw new ConflictException("Username already exists.");
        });

        var growGuru = this.growGuruRepo.save(req.toGrowGuru(encoder, generateVerificationCode()));

        var space = spaceRepository.save(
                Space.builder()
                        .name(String.format("%s's space", growGuru.getUsername()))
                        .admins(new HashSet<>(Set.of(growGuru)))
                        .build()
        );
        growGuru.getAdminSpaces().add(space);

        productRepository.findAllDefaultProducts().forEach(product -> productService.buyProduct(product.getId(), growGuru));
        mailService.sendVerificationMail(growGuru.getEmail(), growGuru.getUsername(), growGuru.getVerificationCode());

        return growGuru;
    }

    public String verify(String code) {
        var growGuru = growGuruRepo.findUserByVerificationCode(code).orElseThrow(
                () -> new ResourceNotFound("No verification found with that code"));

        if (growGuru.isEnabled()){
            return "Account has been already verified";
        }

        growGuru.setEnabled(true);
        return "Verification-success";
    }

    public void remove(GrowGuru growGuru) {
        growGuru.getAllSpaces().forEach(s -> spaceService.removeMember(s.getId(), growGuru.getUsername()));
        trackerRepository.deleteSessionsByGrowGuruId(growGuru.getId());
        this.growGuruRepo.delete(growGuru);
    }

    public GrowGuru update(GrowGuru.UpdateDto updateDto) {
        GrowGuru growGuru = SecurityContextUtil.getCurrentGrowGuru();

        updateUsernameIfPresent(updateDto, growGuru);
        updateEmailIfPresent(updateDto, growGuru);
        updatePasswordIfPresent(updateDto, growGuru);
        updatePictureIfPresent(updateDto, growGuru);

        return growGuruRepo.save(growGuru);
    }

    private void updateUsernameIfPresent(GrowGuru.UpdateDto updateDto, GrowGuru growGuru) {
        Optional.ofNullable(updateDto.getUsername())
                .filter(StringUtils::hasText)
                .ifPresent(growGuru::setUsername);
    }

    private void updateEmailIfPresent(GrowGuru.UpdateDto updateDto, GrowGuru growGuru) {
        Optional.ofNullable(updateDto.getEmail())
                .filter(StringUtils::hasText)
                .ifPresent(growGuru::setEmail);
    }

    private void updatePasswordIfPresent(GrowGuru.UpdateDto updateDto, GrowGuru growGuru) {
        if (hasText(updateDto.getPassword())) growGuru.setPassword(encoder.encode(updateDto.getPassword()));
    }

    private void updatePictureIfPresent(GrowGuru.UpdateDto updateDto, GrowGuru growGuru) {
        if (updateDto.getPicture() != null) growGuru.setPicture(updateDto.getPicture());
    }

    private String generateVerificationCode() {
        var code = RandomString.make(24);
        return growGuruRepo.existsByVerificationCode(code) ? generateVerificationCode() : code;
    }
}
