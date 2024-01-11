package com.digytal.control.service.modulo.acesso;

import com.digytal.control.infra.business.RegistroDuplicadoException;
import com.digytal.control.infra.business.RegistroIncompativelException;
import com.digytal.control.infra.business.RegistroNaoLocalizadoException;
import com.digytal.control.infra.commons.validation.Entities;
import com.digytal.control.infra.commons.validation.Validations;
import com.digytal.control.model.modulo.acesso.empresa.conta.ContaEntity;
import com.digytal.control.model.modulo.acesso.empresa.conta.ContaRequest;
import com.digytal.control.model.modulo.acesso.empresa.conta.ContaResponse;
import com.digytal.control.model.modulo.acesso.empresa.pagamento.FormaPagamentoEntity;
import com.digytal.control.model.modulo.acesso.empresa.pagamento.FormaPagamentoCadastroRequest;
import com.digytal.control.model.modulo.acesso.empresa.pagamento.FormaPagamentoCadastroResponse;
import com.digytal.control.model.comum.MeioPagamento;
import com.digytal.control.model.modulo.cadastro.CadastroResponse;
import com.digytal.control.model.modulo.cadastro.produto.ProdutoEntity;
import com.digytal.control.model.modulo.cadastro.produto.ProdutoResponse;
import com.digytal.control.repository.modulo.acesso.empresa.ContaRepository;
import com.digytal.control.repository.modulo.acesso.empresa.FormaPagamentoRepository;
import com.digytal.control.service.comum.AbstractService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ContaServiceTest {
    @Mock
    private ContaRepository contaRepository;

    @Mock
    private FormaPagamentoRepository formaPagamentoRepository;

    @InjectMocks
    private ContaService contaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBuscarConta() {
        // Arrange
        Integer id = 1;
        ContaEntity contaEntity = new ContaEntity();
        contaEntity.setId(id);
        contaEntity.setNome("Conta Teste");
        contaEntity.setSaldo(100.0);

        when(contaRepository.findById(id)).thenReturn(Optional.of(contaEntity));

        // Act
        ContaResponse contaResponse = contaService.buscarConta(id);

        // Assert
        assertNotNull(contaResponse);
        assertEquals(id, contaResponse.getId());
        assertEquals("Conta Teste", contaResponse.getNome());
        assertEquals(100.0, contaResponse.getSaldo());
    }

    @Test
    void testBuscarConta_ThrowsRegistroNaoLocalizadoException() {
        // Arrange
        Integer id = 1;

        when(contaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RegistroNaoLocalizadoException.class, () -> contaService.buscarConta(id));
    }

    @Test
    void testConsultarFormasPagamento() {
        // Arrange
        Integer contaId = 1;
        List<FormaPagamentoEntity> formaPagamentoEntities = new ArrayList<>();
        FormaPagamentoEntity formaPagamentoEntity1 = new FormaPagamentoEntity();
        formaPagamentoEntity1.setId(1);
        formaPagamentoEntity1.setDescricao("Forma Pagamento 1");
        formaPagamentoEntity1.setTaxa(0.0);
        formaPagamentoEntities.add(formaPagamentoEntity1);

        FormaPagamentoEntity formaPagamentoEntity2 = new FormaPagamentoEntity();
        formaPagamentoEntity2.setId(2);
        formaPagamentoEntity2.setDescricao("Forma Pagamento 2");
        formaPagamentoEntity2.setTaxa(1.5);
        formaPagamentoEntities.add(formaPagamentoEntity2);

        when(formaPagamentoRepository.findByConta(contaId)).thenReturn(formaPagamentoEntities);

        // Act
        List<FormaPagamentoCadastroResponse> formasPagamento = contaService.consultarFormasPagamento(contaId);

        // Assert
        assertNotNull(formasPagamento);
        assertEquals(2, formasPagamento.size());
    }
}

        FormaPagamentoCadastroResponse formaPagamento1 = formasPagamento.get(0);
        assertEquals(1, formaPagamento1.getId());
        assertEquals("Forma Pagamento 1", formaPagamento1.getDescricao());
        assertEquals(0.0, formaPagamento1.getTaxa());

        FormaPagamentoCadastroResponse formaPagamento2 = formasPagamento.get(1);
        assertEquals(2, formaPagamento2.getId());
        assertEquals("Forma Pagamento 2", formaPagamento2.getDescricao());
        assertEquals(1.5, formaPagamento2.getTaxa());
    }

    @Test
    void testIncluirFormaPagamento() {
        // Arrange
        Integer contaId = 1;
        FormaPagamentoCadastroRequest formaPagamentoCadastroRequest = new FormaPagamentoCadastroRequest();
        formaPagamentoCadastroRequest.setDescricao("Forma Pagamento Teste");
        formaPagamentoCadastroRequest.setTaxa(2.0);

        ContaEntity contaEntity = new ContaEntity();
        contaEntity.setId(contaId);

        when(contaRepository.findById(contaId)).thenReturn(Optional.of(contaEntity));
        when(formaPagamentoRepository.existsByEmpresaAndMeioPagamentoAndNumeroParcelas(any(), any(), anyInt())).thenReturn(false);

        // Act
        Integer formaPagamentoId = contaService.incluirFormaPagamento(contaId, formaPagamentoCadastroRequest);

        // Assert
        assertNotNull(formaPagamentoId);
        verify(formaPagamentoRepository, times(1)).save(any(FormaPagamentoEntity.class));
    }

    @Test
    void testIncluirFormaPagamento_ThrowsRegistroNaoLocalizadoException() {
        // Arrange
        Integer contaId = 1;
        FormaPagamentoCadastroRequest formaPagamentoCadastroRequest = new FormaPagamentoCadastroRequest();
        formaPagamentoCadastroRequest.setDescricao("Forma Pagamento Teste");
        formaPagamentoCadastroRequest.setTaxa(2.0);

        when(contaRepository.findById(contaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RegistroNaoLocalizadoException.class, () -> contaService.incluirFormaPagamento(contaId, formaPagamentoCadastroRequest));
    }

    @Test
    void testIncluirFormaPagamento_ThrowsRegistroDuplicadoException() {
        // Arrange
        Integer contaId = 1;
        FormaPagamentoCadastroRequest formaPagamentoCadastroRequest = new FormaPagamentoCadastroRequest();
        formaPagamentoCadastroRequest.setDescricao("Forma Pagamento Teste");
        formaPagamentoCadastroRequest.setTaxa(2.0);

        ContaEntity contaEntity = new ContaEntity();
        contaEntity.setId(contaId);

        when(contaRepository.findById(contaId)).thenReturn(Optional.of(contaEntity));
        when(formaPagamentoRepository.existsByEmpresaAndMeioPagamentoAndNumeroParcelas(any(), any(), anyInt())).thenReturn(true);

        // Act & Assert
        assertThrows(RegistroDuplicadoException.class, () -> contaService.incluirFormaPagamento(contaId, formaPagamentoCadastroRequest));
    }

    @Test
    void testExcluirFormaPagamento() {
        // Arrange
        Integer formaPagamentoId = 1;

        when(formaPagamentoRepository.existsById(formaPagamentoId)).thenReturn(true);

        // Act
        boolean result = contaService.excluirFormaPagamento(formaPagamentoId);

        // Assert
        assertTrue(result);
        verify(formaPagamentoRepository, times(1)).deleteById(formaPagamentoId);
    }

    @Test
    void testExcluirFormaPagamento_ThrowsRegistroNaoLocalizadoException() {
        // Arrange
        Integer formaPagamentoId = 1;

        when(formaPagamentoRepository.existsById(formaPagamentoId)).thenReturn(false);

        // Act & Assert
        assertThrows(RegistroNaoLocalizadoException.class, () -> contaService.excluirFormaPagamento(formaPagamentoId));
    }
}