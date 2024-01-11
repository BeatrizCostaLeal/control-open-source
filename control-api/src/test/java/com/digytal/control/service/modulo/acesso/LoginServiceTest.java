package com.digytal.control.service.modulo.acesso;

import com.digytal.control.infra.business.login.LoginException;
import com.digytal.control.infra.business.login.SenhaExpiradaException;
import com.digytal.control.infra.business.login.UsuarioBloqueadoException;
import com.digytal.control.infra.commons.validation.Validations;
import com.digytal.control.infra.model.*;
import com.digytal.control.infra.model.usuario.UsuarioCadastroResponse;
import com.digytal.control.infra.model.usuario.UsuarioEmpresaResponse;
import com.digytal.control.infra.security.jwt.JwtCreator;
import com.digytal.control.infra.security.jwt.JwtObject;
import com.digytal.control.infra.security.jwt.SecurityConfig;
import com.digytal.control.infra.model.usuario.EmpresaSimplificadaResponse;
import com.digytal.control.model.modulo.acesso.usuario.UsuarioEntity;
import com.digytal.control.repository.modulo.acesso.UsuarioRepository;
import com.digytal.control.repository.modulo.acesso.empresa.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServiceTest {
    @Mock
    private UsuarioRepository repository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAutenticar_ValidCredentials_ReturnsSessaoResponse() {
        // Arrange
        String username = "testuser";
        String password = "testpassword";
        LoginRequest loginRequest = new LoginRequest(username, password);

        UsuarioEntity usuarioEntity = new UsuarioEntity();
        usuarioEntity.setId(1L);
        usuarioEntity.setLogin(username);
        usuarioEntity.setSenha(encoder.encode(password));
        usuarioEntity.setExpirado(false);
        usuarioEntity.setBloqueado(false);
        usuarioEntity.setCadastro(new UsuarioCadastroResponse());

        when(repository.findByLogin(username)).thenReturn(usuarioEntity);
        when(encoder.matches(password, usuarioEntity.getSenha())).thenReturn(true);

        // Act
        SessaoResponse sessaoResponse = loginService.autenticar(loginRequest);

        // Assert
        assertNotNull(sessaoResponse);
        assertNotNull(sessaoResponse.getToken());
        assertNotNull(sessaoResponse.getInicioSessao());
        assertNotNull(sessaoResponse.getFimSessao());
        assertNotNull(sessaoResponse.getUsuario());
        assertTrue(sessaoResponse.getUsuario() instanceof UsuarioCadastroResponse);
        assertEquals(username, sessaoResponse.getUsuario().getLogin());
        assertFalse(sessaoResponse.getUsuario().getEmpresas().isEmpty());
        assertEquals(1, sessaoResponse.getUsuario().getEmpresas().size());
    }

    @Test
    void testAutenticar_InvalidCredentials_ThrowsLoginException() {
        // Arrange
        String username = "testuser";
        String password = "testpassword";
        LoginRequest loginRequest = new LoginRequest(username, password);

        when(repository.findByLogin(username)).thenReturn(null);

        // Act & Assert
        assertThrows(LoginException.class, () -> loginService.autenticar(loginRequest));
    }

    @Test
    void testAutenticar_IncorrectPassword_ThrowsLoginException() {
        // Arrange
        String username = "testuser";
        String password = "testpassword";
        LoginRequest loginRequest = new LoginRequest(username, password);

        UsuarioEntity usuarioEntity = new UsuarioEntity();
        usuarioEntity.setId(1L);
        usuarioEntity.setLogin(username);
        usuarioEntity.setSenha(encoder.encode("incorrectpassword"));

        when(repository.findByLogin(username)).thenReturn(usuarioEntity);
        when(encoder.matches(password, usuarioEntity.getSenha())).thenReturn(false);

        // Act & Assert
        assertThrows(LoginException.class, () -> loginService.autenticar(loginRequest));
    }

    @Test
    void testAutenticar_ExpiredPassword_ThrowsSenhaExpiradaException() {
        // Arrange
        String username = "testuser";
        String password = "testpassword";
        LoginRequest loginRequest = new LoginRequest(username, password);

        UsuarioEntity usuarioEntity = new UsuarioEntity();
        usuarioEntity.setId(1L);
        usuarioEntity.setLogin(username);
        usuarioEntity.setSenha(encoder.encode(password));
        usuarioEntity.setExpirado(true);

        when(repository.findByLogin(username)).thenReturn(usuarioEntity);
        when(encoder.matches(password, usuarioEntity.getSenha())).thenReturn(true);

        // Act & Assert
        assertThrows(SenhaExpiradaException.class, () -> loginService.autenticar(loginRequest));
    }

    @Test
    void testAutenticar_BlockedUser_ThrowsUsuarioBloqueadoException() {
        // Arrange
        String username = "testuser";
        String password = "testpassword";
        LoginRequest loginRequest = new LoginRequest(username, password);

        UsuarioEntity usuarioEntity = new UsuarioEntity();
        usuarioEntity.setId(1L);
        usuarioEntity.setLogin(username);
        usuarioEntity.setSenha(encoder.encode(password));
        usuarioEntity.setBloqueado(true);

        when(repository.findByLogin(username)).thenReturn(usuarioEntity);
        when(encoder.matches(password, usuarioEntity.getSenha())).thenReturn(true);

        // Act & Assert
        assertThrows(UsuarioBloqueadoException.class, () -> loginService.autenticar(loginRequest));
    }
}