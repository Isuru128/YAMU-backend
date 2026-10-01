package lk.accountservice.service;

import lk.accountservice.config.JwtService;
import lk.accountservice.dto.AccountResponse;
import lk.accountservice.dto.AuthResponse;
import lk.accountservice.dto.LoginRequest;
import lk.accountservice.dto.RegisterRequest;
import lk.accountservice.dto.UpdateStatusRequest;
import lk.accountservice.exception.AccountSuspendedException;
import lk.accountservice.exception.EmailAlreadyExistsException;
import lk.accountservice.exception.InvalidCredentialsException;
import lk.accountservice.model.Account;
import lk.accountservice.model.AccountStatus;
import lk.accountservice.model.Profile;
import lk.accountservice.model.Role;
import lk.accountservice.repository.AccountRepository;
import lk.accountservice.repository.AccountStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountStatusHistoryRepository statusHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AccountService accountService;

    private Account sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = Account.builder()
                .id("acc-100")
                .firstName("Kamal")
                .lastName("Perera")
                .email("kamal@example.com")
                .phoneNumber("0771234567")
                .passwordHash("encoded_pwd")
                .role(Role.PASSENGER)
                .status(AccountStatus.ACTIVE)
                .profile(Profile.builder().build())
                .build();
    }

    @Test
    @DisplayName("Successfully register passenger account")
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Kamal")
                .lastName("Perera")
                .email("kamal@example.com")
                .phoneNumber("0771234567")
                .password("secret123")
                .role(Role.PASSENGER)
                .build();

        when(accountRepository.existsByEmail("kamal@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_pwd");
        when(accountRepository.save(any(Account.class))).thenReturn(sampleAccount);
        when(jwtService.generateToken(any(Account.class))).thenReturn("mock-jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = accountService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getAccount().getEmail()).isEqualTo("kamal@example.com");
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Fail register when email already registered (Negative Scenario)")
    void register_EmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Kamal")
                .lastName("Perera")
                .email("kamal@example.com")
                .password("secret123")
                .phoneNumber("0771234567")
                .build();

        when(accountRepository.existsByEmail("kamal@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> accountService.register(request));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Successfully login with valid credentials")
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("kamal@example.com")
                .password("secret123")
                .build();

        when(accountRepository.findByEmail("kamal@example.com")).thenReturn(Optional.of(sampleAccount));
        when(passwordEncoder.matches("secret123", "encoded_pwd")).thenReturn(true);
        when(jwtService.generateToken(sampleAccount)).thenReturn("valid-jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = accountService.login(request);

        assertThat(response.getToken()).isEqualTo("valid-jwt-token");
        assertThat(response.getAccount().getId()).isEqualTo("acc-100");
    }

    @Test
    @DisplayName("Fail login with invalid password (Negative Scenario)")
    void login_InvalidPassword() {
        LoginRequest request = LoginRequest.builder()
                .email("kamal@example.com")
                .password("wrongpassword")
                .build();

        when(accountRepository.findByEmail("kamal@example.com")).thenReturn(Optional.of(sampleAccount));
        when(passwordEncoder.matches("wrongpassword", "encoded_pwd")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> accountService.login(request));
    }

    @Test
    @DisplayName("Fail login when account is suspended (Negative Scenario)")
    void login_SuspendedAccount() {
        sampleAccount.setStatus(AccountStatus.SUSPENDED);
        LoginRequest request = LoginRequest.builder()
                .email("kamal@example.com")
                .password("secret123")
                .build();

        when(accountRepository.findByEmail("kamal@example.com")).thenReturn(Optional.of(sampleAccount));
        when(passwordEncoder.matches("secret123", "encoded_pwd")).thenReturn(true);

        assertThrows(AccountSuspendedException.class, () -> accountService.login(request));
    }

    @Test
    @DisplayName("Admin updates user account status")
    void updateStatus_Success() {
        UpdateStatusRequest request = UpdateStatusRequest.builder()
                .status(AccountStatus.SUSPENDED)
                .reason("Suspicious activity")
                .build();

        when(accountRepository.findById("acc-100")).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(sampleAccount);

        AccountResponse response = accountService.updateStatus("acc-100", request, "admin-user");

        assertThat(response.getStatus()).isEqualTo(AccountStatus.SUSPENDED);
        verify(statusHistoryRepository, times(1)).save(any());
    }
}
