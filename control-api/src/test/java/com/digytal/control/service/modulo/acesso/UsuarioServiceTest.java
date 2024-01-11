package com.digytal.control.service.modulo.acesso;

import com.digytal.control.infra.business.BusinessException;
import com.digytal.control.infra.business.CampoObrigatorioException;
import com.digytal.control.infra.business.RegistroNaoLocalizadoException;
import com.digytal.control.infra.business.TamanhoMinimoException;
import com.digytal.control.infra.business.login.DefinicaoSenhaException;
import com.digytal.control.infra.business.login.LoginException;
import com.digytal.control.infra.business.login.TokenInvalidoException;
import com.digytal.control.infra.business.login.UsuarioBloqueadoException;
import com.digytal.control.infra.commons.validation.Entities;
import com.digytal.control.infra.commons.validation.Validation;
import com.digytal.control.infra.email.Message;
import com.digytal.control.infra.email.MessageTemplate;
import com.digytal.control.infra.email.SendEmail;
import com.digytal.control.infra.model.CredenciamentoResponse;
import com.digytal.control.infra.model.LoginRequest;
import com.digytal.control.infra.model.SessaoResponse;
import com.digytal.control.model.modulo.acesso.usuario.SenhaAlteracaoRequest;
import com.digytal.control.model.modulo.acesso.usuario.UsuarioEntity;
import com.digytal.control.repository.modulo.acesso.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {
    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private SendEmail sendEmail;

    @Mock
    private MessageTemplate template;

    @Mock
    private LoginService loginService;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void solicitarNovaSenha_ExistingUser_ReturnsCredenciamentoResponse() {
        // Arrange
        String login = "testuser";
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(1);
        entity.setNome("Test User");
        entity.setEmail("testuser@example.com");

        when(repository.findByLogin(login)).thenReturn(entity);

        // Act
        CredenciamentoResponse response = usuarioService.solicitarNovaSenha(login);

        // Assert
        assertNotNull(response);
        assertEquals(entity.getId(), response.getUsuario());
        assertEquals(entity.getNome(), response.getNome());
        assertNotNull(response.getToken());
        assertNotNull(response.getExpiracao());

        verify(repository, times(1)).findByLogin(login);
        verify(sendEmail, times(1)).sendResetSenha(anyString(), anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void solicitarNovaSenha_NonExistingUser_ThrowsRegistroNaoLocalizadoException() {
        // Arrange
        String login = "nonexistinguser";

        when(repository.findByLogin(login)).thenReturn(null);

        // Act & Assert
        assertThrows(RegistroNaoLocalizadoException.class, () -> {
            usuarioService.solicitarNovaSenha(login);
        });

        verify(repository, times(1)).findByLogin(login);
        verify(sendEmail, never()).sendResetSenha(anyString(), anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void solicitarNovaSenhaById_ValidId_ReturnsCredenciamentoResponse() {
        // Arrange
        Integer id = 1;
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(id);
        entity.setNome("Test User");
        entity.setEmail("testuser@example.com");

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(encoder.encode(anyString())).thenReturn("encodedPassword");
        when(template.redefinicaoSenhaTitulo()).thenReturn("Reset Password");
        when(sendEmail.sendResetSenha(anyString(), anyString(), anyString(), anyInt(), anyString())).thenReturn(true);

        // Act
        CredenciamentoResponse response = usuarioService.solicitarNovaSenha(id);

        // Assert
        assertNotNull(response);
        assertEquals(entity.getId(), response.getUsuario());
        assertEquals(entity.getNome(), response.getNome());
        assertNotNull(response.getToken());
        assertNotNull(response.getExpiracao());

        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(entity);
        verify(sendEmail, times(1)).sendResetSenha(anyString(), anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void solicitarNovaSenhaById_InvalidId_ThrowsRegistroNaoLocalizadoException() {
        // Arrange
        Integer id = 999;

        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RegistroNaoLocalizadoException.class, () -> {
            usuarioService.solicitarNovaSenha(id);
        });

        verify(repository, times(1)).findById(id);
        verify(repository, never()).save(any());
        verify(sendEmail, never()).sendResetSenha(anyString(), anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void alterarSenha_ValidRequest_ReturnsSessaoResponse() {
        // Arrange
        Long expiracao = LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        SenhaAlteracaoRequest request = new SenhaAlteracaoRequest();
        request.setUsuario(1);
        request.setSenhaAtual("oldPassword");
        request.setNovaSenha("newPassword");
        request.setNovaSenhaConfirmacao("newPassword");

        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(request.getUsuario());
        entity.setNome("Test User");
        entity.setEmail("testuser@example.com");
        entity.setSenha("encodedPassword");

        when(repository.findById(request.getUsuario())).thenReturn(Optional.of(entity));
        when(encoder.matches(request.getSenhaAtual(), entity.getSenha())).thenReturn(true);
        when(template.alteracaoSenhaTitulo()).thenReturn("Change Password");
        when(template.alteracaoSenhaMensagem(entity.getNome())).thenReturn("Your password has been changed");
        when(sendEmail.send(any(Message.class))).thenReturn(true);
        when(loginService.autenticar(any(LoginRequest.class))).thenReturn(new SessaoResponse());

        // Act
        SessaoResponse response = usuarioService.alterarSenha(expiracao, request);

        // Assert
        assertNotNull(response);

        verify(repository, times(1)).findById(request.getUsuario());
        verify(repository, times(1)).save(entity);
        verify(sendEmail, times(1)).send(any(Message.class));
        verify(loginService, times(1)).autenticar(any(LoginRequest.class));
    }

    // Add more test cases for other methods in the UsuarioService class
}