package tourplanner.backend.controller;

import tourplanner.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    record AuthRequest(String username, String password) {}
    record RegisterRequest(String username, String email, String password, String confirmPassword) {}
    record LoginResponse(String token, Long userId, String username) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        var result = authService.register(request.username(), request.email(), request.password(), request.confirmPassword());
        if (!result.success()) {
            return ResponseEntity.badRequest().body(Map.of("message", result.message()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", result.message()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        var result = authService.login(request.username(), request.password());
        if (!result.success()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }
        return ResponseEntity.ok(new LoginResponse(result.token(), result.userId(), result.username()));
    }
}