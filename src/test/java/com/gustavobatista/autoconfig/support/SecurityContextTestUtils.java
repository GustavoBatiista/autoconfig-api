package com.gustavobatista.autoconfig.support;

import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Helpers para testes unitários que dependem de {@link SecurityContextHolder}.
 */
public final class SecurityContextTestUtils {

    private SecurityContextTestUtils() {
    }

    /**
     * Simula usuário autenticado com {@code auth.getName() == email}.
     * O teste deve fazer {@code when(userRepository.findByEmail(email)).thenReturn(Optional.of(user))}.
     */
    public static MockedStatic<SecurityContextHolder> mockAuthenticatedUser(String email) {
        MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class);
        SecurityContext context = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        mocked.when(SecurityContextHolder::getContext).thenReturn(context);
        when(context.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        return mocked;
    }

    /** Simula contexto sem autenticação válida ({@code isAuthenticated() == false}). */
    public static MockedStatic<SecurityContextHolder> mockUnauthenticated() {
        MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class);
        SecurityContext context = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        mocked.when(SecurityContextHolder::getContext).thenReturn(context);
        when(context.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        return mocked;
    }

    /** Simula {@code getAuthentication() == null}. */
    public static MockedStatic<SecurityContextHolder> mockNoAuthentication() {
        MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class);
        SecurityContext context = mock(SecurityContext.class);
        mocked.when(SecurityContextHolder::getContext).thenReturn(context);
        when(context.getAuthentication()).thenReturn(null);
        return mocked;
    }
}
