package com.gustavobatista.autoconfig.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.gustavobatista.autoconfig.enums.Role;

import static org.mockito.Mockito.lenient;
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
     * Define {@link Authentication#getAuthorities()} com a mesma {@link Role} que os emails de
     * {@link TestFixtures} (alinhado a {@code CustomUserDetailsService} / JWT).
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
        lenient().when(authentication.getAuthorities()).thenAnswer(invocation -> defaultAuthoritiesForTestEmail(email));
        return mocked;
    }

    /**
     * Mesmo que {@link #mockAuthenticatedUser(String)}, mas com authorities explícitas (ex.: cenários
     * com várias roles ou email fora dos fixtures).
     */
    public static MockedStatic<SecurityContextHolder> mockAuthenticatedUser(String email, Role... roles) {
        MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class);
        SecurityContext context = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        mocked.when(SecurityContextHolder::getContext).thenReturn(context);
        when(context.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        if (roles.length == 0) {
            lenient().when(authentication.getAuthorities())
                    .thenAnswer(invocation -> Collections.<GrantedAuthority>emptyList());
        } else {
            List<GrantedAuthority> list = new ArrayList<>(roles.length);
            for (Role r : roles) {
                list.add(new SimpleGrantedAuthority(r.name()));
            }
            lenient().when(authentication.getAuthorities()).thenAnswer(invocation -> list);
        }
        return mocked;
    }

    private static List<GrantedAuthority> defaultAuthoritiesForTestEmail(String email) {
        if (TestFixtures.ADMIN_EMAIL.equals(email)) {
            return List.of(new SimpleGrantedAuthority(Role.ROLE_ADMIN.name()));
        }
        if (TestFixtures.MANAGER_EMAIL.equals(email)) {
            return List.of(new SimpleGrantedAuthority(Role.ROLE_MANAGER.name()));
        }
        if (TestFixtures.SELLER_EMAIL.equals(email)) {
            return List.of(new SimpleGrantedAuthority(Role.ROLE_SELLER.name()));
        }
        return List.of();
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
