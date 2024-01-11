package com.digytal.control.service.modulo.acesso;

import com.digytal.control.infra.business.CampoObrigatorioException;
import com.digytal.control.infra.business.CpfCnpjInvalidoException;
import com.digytal.control.infra.business.RegistroIncompativelException;
import com.digytal.control.infra.commons.definition.Text;
import com.digytal.control.infra.commons.validation.Validation;
import com.digytal.control.infra.model.CredenciamentoResponse;
import com.digytal.control.infra.model.TipoLogin;
import com.digytal.control.model.modulo.acesso.empresa.EmpresaEntity;
import com.digytal.control.model.modulo.acesso.empresa.aplicacao.AplicacaoEntity;
import com.digytal.control.model.modulo.acesso.empresa.aplicacao.AplicacaoTipo;
import com.digytal.control.model.modulo.acesso.empresa.conta.ContaEntity;
import com.digytal.control.model.modulo.acesso.empresa.pagamento.FormaPagamentoEntity;
import com.digytal.control.model.modulo.acesso.organizacao.OrganizacaoEntity;
import com.digytal.control.model.comum.EntidadeCadastral;
import com.digytal.control.model.comum.MeioPagamento;
import com.digytal.control.model.comum.cadastramento.CadastroSimplificadoRequest;
import com.digytal.control.repository.modulo.acesso.empresa.AplicacaoRepository;
import com.digytal.control.repository.modulo.acesso.empresa.ContaRepository;
import com.digytal.control.repository.modulo.acesso.empresa.FormaPagamentoRepository;
import com.digytal.control.service.comum.CadastroFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class PrimeiroAcessoServiceTest {
    @Mock
    private ContaRepository contaRepository;
    @Mock
    private AplicacaoRepository aplicacaoRepository;
    @Mock
    private FormaPagamentoRepository formaPagamentoRepository;

    @InjectMocks
    private PrimeiroAcessoService primeiroAcessoService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConfigurarPrimeiroAcesso_ValidInput_ReturnsCredenciamentoResponse() {
        // Arrange
        String cpfCnpj = "12345678900";
        CadastroSimplificadoRequest request = new CadastroSimplificadoRequest();
        // Set request properties

        // Act
        CredenciamentoResponse response = primeiroAcessoService.configurarPrimeiroAcesso(cpfCnpj, request);

        // Assert
        assertNotNull(response);
        // Add more assertions based on the expected behavior of the method
    }

    @Test
    public void testConfigurarPrimeiroAcesso_EmptyCpfCnpj_ThrowsCampoObrigatorioException() {
        // Arrange
        String cpfCnpj = "";
        CadastroSimplificadoRequest request = new CadastroSimplificadoRequest();
        // Set request properties

        // Act & Assert
        assertThrows(CampoObrigatorioException.class, () -> {
            primeiroAcessoService.configurarPrimeiroAcesso(cpfCnpj, request);
        });
    }

    @Test
    public void testConfigurarPrimeiroAcesso_InvalidCpfCnpj_ThrowsCpfCnpjInvalidoException() {
        // Arrange
        String cpfCnpj = "123456789";
        CadastroSimplificadoRequest request = new CadastroSimplificadoRequest();
        // Set request properties

        // Act & Assert
        assertThrows(CpfCnpjInvalidoException.class, () -> {
            primeiroAcessoService.configurarPrimeiroAcesso(cpfCnpj, request);
        });
    }

    @Test
    public void testCadastrarOrganizacao_ValidInput_ReturnsOrganizacaoEntity() {
        // Arrange
        String cpfCnpj = "12345678900";
        String nome = "Test Organization";
        String email = "test@example.com";

        // Act
        OrganizacaoEntity organizacaoEntity = primeiroAcessoService.cadastrarOrganizacao(cpfCnpj, nome, email);

        // Assert
        assertNotNull(organizacaoEntity);
        // Add more assertions based on the expected behavior of the method
    }

    // Add more test methods for other methods in the PrimeiroAcessoService class

    // You can also use Mockito to mock dependencies and verify interactions with them
    @Test
    public void testCadastrarContaFisica_ValidInput_SavesContaEntity() {
        // Arrange
        boolean pessoaJuridica = false;
        Integer empresa = 1;

        // Act
        primeiroAcessoService.cadastrarContaFisica(pessoaJuridica, empresa);

        // Assert
        verify(contaRepository, times(1)).save(any(ContaEntity.class));
    }

    @Test
    public void testCadastrarAplicacoes_ValidInput_SavesAplicacaoEntities() {
        // Arrange
        Integer organizacao = 1;

        // Act
        primeiroAcessoService.cadastrarAplicacoes(organizacao);

        // Assert
        verify(aplicacaoRepository, times(2)).save(any(AplicacaoEntity.class));
    }

    @Test
    public void testCadastrarContaFisicaPagamento_ValidInput_SavesFormaPagamentoEntity() {
        // Arrange
        Integer conta = 1;
        Integer empresa = 1;

        // Act
        primeiroAcessoService.cadastrarContaFisicaPagamento(conta, empresa);

        // Assert
        verify(formaPagamentoRepository, times(1)).save(any(FormaPagamentoEntity.class));
    }

    // Add more test methods for other methods in the PrimeiroAcessoService class
}