package com.example.student_system.musicproject.services;

import com.example.student_system.musicproject.entities.User;
import com.example.student_system.musicproject.repository.UserRepository;
import com.example.student_system.musicproject.services.classes.AuthServiceImpl;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FirebaseToken firebaseToken;

    @Mock
    private FirebaseAuthException firebaseAuthException;

    @InjectMocks
    private AuthServiceImpl authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setFirebaseUid("firebase-uid");
        existingUser.setEmail("test@example.com");
    }

    @Nested
    @DisplayName("Verify Token Tests")
    class VerifyTokenTests {

        @Test
        @DisplayName("verifyToken - Success")
        void testVerifyToken_Success() throws FirebaseAuthException {
            when(firebaseAuth.verifyIdToken("valid-token"))
                    .thenReturn(firebaseToken);

            FirebaseToken result = authService.verifyToken("valid-token");

            assertNotNull(result);
            assertEquals(firebaseToken, result);

            verify(firebaseAuth).verifyIdToken("valid-token");
        }

        @Test
        @DisplayName("verifyToken - Firebase Error")
        void testVerifyToken_FirebaseError() throws FirebaseAuthException {
            when(firebaseAuth.verifyIdToken("invalid-token"))
                    .thenThrow(firebaseAuthException);

            assertThrows(
                    FirebaseAuthException.class,
                    () -> authService.verifyToken("invalid-token")
            );

            verify(firebaseAuth).verifyIdToken("invalid-token");
        }
    }

    @Nested
    @DisplayName("Find Or Create Local User Tests")
    class FindOrCreateLocalUserTests {

        @Test
        @DisplayName("findOrCreateLocalUser - Existing User")
        void testFindOrCreateLocalUser_ExistingUser() {
            when(firebaseToken.getUid())
                    .thenReturn("firebase-uid");

            when(userRepository.findByFirebaseUid("firebase-uid"))
                    .thenReturn(existingUser);

            User result = authService.findOrCreateLocalUser(firebaseToken);

            assertNotNull(result);
            assertEquals(existingUser, result);
            assertEquals("firebase-uid", result.getFirebaseUid());
            assertEquals("test@example.com", result.getEmail());

            verify(userRepository).findByFirebaseUid("firebase-uid");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("findOrCreateLocalUser - Create New User")
        void testFindOrCreateLocalUser_CreateNewUser() {
            when(firebaseToken.getUid())
                    .thenReturn("firebase-uid");

            when(firebaseToken.getEmail())
                    .thenReturn("test@example.com");

            when(userRepository.findByFirebaseUid("firebase-uid"))
                    .thenReturn(null);

            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            User result = authService.findOrCreateLocalUser(firebaseToken);

            assertNotNull(result);
            assertEquals("firebase-uid", result.getFirebaseUid());
            assertEquals("test@example.com", result.getEmail());

            verify(userRepository).findByFirebaseUid("firebase-uid");
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("logout - Success")
        void testLogout_Success() throws FirebaseAuthException {
            doNothing()
                    .when(firebaseAuth)
                    .revokeRefreshTokens("firebase-uid");

            authService.logout("firebase-uid");

            verify(firebaseAuth).revokeRefreshTokens("firebase-uid");
        }

        @Test
        @DisplayName("logout - Firebase Error")
        void testLogout_FirebaseError() throws FirebaseAuthException {
            doThrow(firebaseAuthException)
                    .when(firebaseAuth)
                    .revokeRefreshTokens("firebase-uid");

            assertThrows(
                    FirebaseAuthException.class,
                    () -> authService.logout("firebase-uid")
            );

            verify(firebaseAuth).revokeRefreshTokens("firebase-uid");
        }
    }
}