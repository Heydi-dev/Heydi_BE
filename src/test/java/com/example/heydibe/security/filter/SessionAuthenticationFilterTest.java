package com.example.heydibe.security.filter;

import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static com.example.heydibe.security.util.SessionKeys.LOGIN_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionAuthenticationFilterTest {

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void longLoginUser_isAuthenticated() throws Exception {
        SessionAuthenticationFilter filter = new SessionAuthenticationFilter(userRepository);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws/conversations");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(LOGIN_USER, 7L);
        request.setSession(session);

        when(userRepository.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(User.builder().id(7L).build()));

        filter.doFilter(request, response, new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(7L);
        verify(userRepository).findByIdAndDeletedAtIsNull(7L);
    }

    @Test
    void stringLoginUser_isNotAuthenticated() throws Exception {
        SessionAuthenticationFilter filter = new SessionAuthenticationFilter(userRepository);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws/conversations");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(LOGIN_USER, "9");
        request.setSession(session);

        filter.doFilter(request, response, new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        verify(userRepository, never()).findByIdAndDeletedAtIsNull(9L);
    }
}
