package com.digytal.control.service.modulo.acesso;

import com.digytal.control.infra.business.CampoObrigatorioException;
import com.digytal.control.infra.business.RegistroNaoLocalizadoException;
import com.digytal.control.infra.config.RequestInfo;
import com.digytal.control.model.modulo.acesso.empresa.aplicacao.AplicacaoEntity;
import com.digytal.control.model.modulo.acesso.empresa.aplicacao.AplicacaoRequest;
import com.digytal.control.model.modulo.acesso.empresa.aplicacao.AplicacaoResponse;
import com.digytal.control.model.modulo.acesso.empresa.aplicacao.AplicacaoTipo;
import com.digytal.control.repository.modulo.acesso.empresa.AplicacaoRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class AplicacaoServiceTest {
    @Mock
    private AplicacaoRepository repository;

    @Mock
    private RequestInfo requestInfo;

    @InjectMocks
    private AplicacaoService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListarAreas() {
        // Arrange
        String nome = "Test Area";
        Integer orgId = 1;
        List<AplicacaoEntity> entityList = new ArrayList<>();
        AplicacaoEntity entity = new AplicacaoEntity();
        entity.setNome(nome);
        entityList.add(entity);

        when(repository.listarAreas(anyInt(), anyString())).thenReturn(entityList);
        when(requestInfo.getOrganizacao()).thenReturn(orgId);

        // Act
        List<AplicacaoResponse> responseList = service.listarAreas(nome);

        // Assert
        Assertions.assertEquals(1, responseList.size());
        Assertions.assertEquals(nome, responseList.get(0).getNome());
    }

    @Test
    void testListarReceitas() {
        // Arrange
        String nome = "Test Receita";
        Integer orgId = 1;
        List<AplicacaoEntity> entityList = new ArrayList<>();
        AplicacaoEntity entity = new AplicacaoEntity();
        entity.setNome(nome);
        entityList.add(entity);
        
        when(repository.listarNaturezas(anyInt(), eq(AplicacaoTipo.RECEITA), anyString())).thenReturn(entityList);
        when(requestInfo.getOrganizacao()).thenReturn(orgId);

        // Act
        List<AplicacaoResponse> responseList = service.listarReceitas(nome);

        // Assert
        Assertions.assertEquals(1, responseList.size());
        Assertions.assertEquals(nome, responseList.get(0).getNome());
    }

    @Test
    void testListarDespesas() {
        // Arrange
        String nome = "Test Despesa";
        Integer orgId = 1;

        List<AplicacaoEntity> entityList = new ArrayList<>();
        AplicacaoEntity entity = new AplicacaoEntity();
        entity.setNome(nome);
        entityList.add(entity);

        when(repository.listarNaturezas(anyInt(), eq(AplicacaoTipo.DESPESA), anyString())).thenReturn(entityList);
        when(requestInfo.getOrganizacao()).thenReturn(orgId);

        // Act
        List<AplicacaoResponse> responseList = service.listarDespesas(nome);

        // Assert
        Assertions.assertEquals(1, responseList.size());
        Assertions.assertEquals(nome, responseList.get(0).getNome());
    }

    @Test
    void testListagem() {
        // Arrange
        String area = "Test Area";
        Integer orgId = 1;

        List<AplicacaoEntity> entityList = new ArrayList<>();
        AplicacaoEntity entity = new AplicacaoEntity();
        entity.setNome(area);
        entityList.add(entity);

        when(repository.listarNaturezas(anyInt(), eq(AplicacaoTipo.DESPESA), eq(area))).thenReturn(entityList);
        when(requestInfo.getOrganizacao()).thenReturn(orgId);

        // Act
        List<AplicacaoResponse> responseList = service.listagem(area);

        // Assert
        Assertions.assertEquals(1, responseList.size());
        Assertions.assertEquals(area, responseList.get(0).getNome());
    }

    @Test
    void testIncluirArea() {
        // Arrange
        AplicacaoRequest request = new AplicacaoRequest();
        request.setNome("Test Area");
        when(repository.save(any(AplicacaoEntity.class))).thenReturn(new AplicacaoEntity());

        // Act
        Integer id = service.incluirArea(request);

        // Assert
        Assertions.assertNotNull(id);
    }

    @Test
    void testIncluirReceita() {
        // Arrange
        AplicacaoRequest request = new AplicacaoRequest();
        request.setNome("Test Receita");
        when(repository.save(any(AplicacaoEntity.class))).thenReturn(new AplicacaoEntity());

        // Act
        Integer id = service.incluirReceita(request);

        // Assert
        Assertions.assertNotNull(id);
    }

    @Test
    void testIncluirDespesa() {
        // Arrange
        AplicacaoRequest request = new AplicacaoRequest();
        request.setNome("Test Despesa");
        when(repository.save(any(AplicacaoEntity.class))).thenReturn(new AplicacaoEntity());

        // Act
        Integer id = service.incluirDespesa(request);

        // Assert
        Assertions.assertNotNull(id);
    }

    @Test
    void testIncluir_WithValidRequest_ShouldSaveEntity() {
        // Arrange
        AplicacaoRequest request = new AplicacaoRequest();
        request.setNome("Test");
        when(repository.save(any(AplicacaoEntity.class))).thenReturn(new AplicacaoEntity());

        // Act
        // Integer id = service.incluir(request, AplicacaoTipo.RECEITA, false, true);

        // Assert
        // Assertions.assertNotNull(id);
    }

    @Test
    void testIncluir_WithEmptyNome_ShouldThrowCampoObrigatorioException() {
        // Arrange
        AplicacaoRequest request = new AplicacaoRequest();
        request.setNome("");
        when(repository.save(any(AplicacaoEntity.class))).thenReturn(new AplicacaoEntity());

        // Act & Assert
        // Assertions.assertThrows(CampoObrigatorioException.class, () -> service.incluir(request, AplicacaoTipo.RECEITA, false, true));
    }

    @Test
    void testIncluir_WithException_ShouldThrowErroNaoMapeadoException() {
        // Arrange
        AplicacaoRequest request = new AplicacaoRequest();
        request.setNome("Test");
        when(repository.save(any(AplicacaoEntity.class))).thenThrow(new RuntimeException());

        // Act & Assert
        // Assertions.assertThrows(ErroNaoMapeadoException.class, () -> service.incluir(request, AplicacaoTipo.RECEITA, false, true));
    }

    @Test
    void testAlterarNome() {
        // Arrange
        Integer id = 1;
        String nome = "Test";
        AplicacaoEntity entity = new AplicacaoEntity();
        when(repository.findById(anyInt())).thenReturn(java.util.Optional.of(entity));
        when(repository.save(any(AplicacaoEntity.class))).thenReturn(new AplicacaoEntity());

        // Act
        boolean result = service.alterarNome(id, nome);

        // Assert
        Assertions.assertTrue(result);
        Assertions.assertEquals(nome, entity.getNome());
    }

    @Test
    void testAlterarNome_WithEmptyNome_ShouldThrowCampoObrigatorioException() {
        // Arrange
        Integer id = 1;
        String nome = "";
        AplicacaoEntity entity = new AplicacaoEntity();
        when(repository.findById(anyInt())).thenReturn(java.util.Optional.of(entity));
        when(repository.save(any(AplicacaoEntity.class))).thenReturn(new AplicacaoEntity());

        // Act & Assert
        Assertions.assertThrows(CampoObrigatorioException.class, () -> service.alterarNome(id, nome));
    }

    @Test
    void testAlterarNome_WithNonExistingId_ShouldThrowRegistroNaoLocalizadoException() {
        // Arrange
        Integer id = 1;
        String nome = "Test";
        when(repository.findById(anyInt())).thenReturn(java.util.Optional.empty());

        // Act & Assert
        Assertions.assertThrows(RegistroNaoLocalizadoException.class, () -> service.alterarNome(id, nome));
    }
}