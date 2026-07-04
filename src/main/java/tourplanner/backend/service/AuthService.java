package tourplanner.backend.service;

import tourplanner.backend.persistence.entity.User;
import tourplanner.backend.persistence.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public record RegisterResult(boolean success, String message) {}
    public record LoginResult(boolean success, String token, Long userId, String username) {}

    public RegisterResult register(String username, String email, String password, String confirmPassword) {
        if (username == null || username.isBlank()) {
            return new RegisterResult(false, "Username is required");
        }
        if (email == null || email.isBlank()) {
            return new RegisterResult(false, "Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new RegisterResult(false, "Email format is invalid");
        }
        if (password == null || password.length() < 6) {
            return new RegisterResult(false, "Password must be at least 6 characters");
        }
        if (!password.equals(confirmPassword)) {
            return new RegisterResult(false, "Passwords do not match");
        }
        if (userRepository.existsByUsername(username)) {
            return new RegisterResult(false, "Username already taken");
        }
        if (userRepository.existsByEmail(email)) {
            return new RegisterResult(false, "Email already registered");
        }

        User user = new User(username, email, passwordEncoder.encode(password));
        userRepository.save(user);
        return new RegisterResult(true, "Registration successful");
    }

    public LoginResult login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return new LoginResult(false, null, null, null);
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return new LoginResult(false, null, null, null);
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return new LoginResult(true, token, user.getId(), user.getUsername());
    }
}