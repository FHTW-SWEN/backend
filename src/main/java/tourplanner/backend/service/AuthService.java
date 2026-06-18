package tourplanner.backend.service;

import tourplanner.backend.persistence.entity.User;
import tourplanner.backend.persistence.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public record RegisterResult(boolean success, String message) {}
    public record LoginResult(boolean success, String token, Long userId, String username) {}

    public RegisterResult register(String username, String password) {
        if (username == null || username.isBlank()) {
            return new RegisterResult(false, "Username is required");
        }
        if (password == null || password.length() < 6) {
            return new RegisterResult(false, "Password must be at least 6 characters");
        }
        if (userRepository.existsByUsername(username)) {
            return new RegisterResult(false, "Username already taken");
        }

        User user = new User(username, passwordEncoder.encode(password));
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
