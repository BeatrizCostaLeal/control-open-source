package com.digytal.control.service.modulo.financeiro;

import com.digytal.control.infra.business.CampoObrigatorioException;
import com.digytal.control.infra.business.RegistroNaoLocalizadoException;
import com.digytal.control.infra.commons.validation.Entities;
import com.digytal.control.infra.commons.validation.Validation;
import com.digytal.control.integracao.asaas.IntegradorPagamento;
import com.digytal.control.integracao.asaas.model.BoletoRequest;
import com.digytal.control.integracao.asaas.model.BoletoResponse;
import com.digytal.control.integracao.asaas.model.Cadastro;
import com.digytal.control.model.comum.cadastramento.CadastroIntegracao;
import com.digytal.control.model.modulo.cadastro.CadastroEntity;
import com.digytal.control.model.modulo.financeiro.parcelamento.ParcelamentoEntity;
import com.digytal.control.model.modulo.financeiro.parcelamento.parcela.ParcelaEntity;
import com.digytal.control.repository.modulo.cadastro.CadastroRepository;
import com.digytal.control.repository.modulo.fincanceiro.ParcelaPagamentoRepository;
import com.digytal.control.repository.modulo.fincanceiro.ParcelaRepository;
import com.digytal.control.repository.modulo.fincanceiro.ParcelamentoRepository;
import com.digytal.control.model.modulo.financeiro.parcelamento.boleto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import static com.digytal.control.infra.commons.validation.Attributes.ID;

@Service
public class BoletoService {
    @Autowired
    private ParcelaRepository parcelaRepository;
    @Autowired
    private ParcelamentoRepository parcelamentoRepository;
    @Autowired
    private CadastroRepository cadastroRepository;
    @Autowired
    private IntegradorPagamento integrador;
    @Autowired
    private PagamentoService lancamentoService;
    @Autowired
    private ParcelaPagamentoRepository parcelaPagamentoRepository;

    @Transactional
    public BoletoResponse gerarBoleto(Integer parcelaId,Double valorBoleto){
        return gerarBoleto(parcelaId, valorBoleto,null);
    }
    @Transactional
    public BoletoResponse gerarBoleto(Integer parcelaId, Double valorBoleto, LocalDate dataVencimento){
        ParcelaEntity parcela = parcelaRepository.findById(parcelaId).orElseThrow(()-> new RegistroNaoLocalizadoException(Entities.PARCELA, ID));
        ParcelamentoEntity parcelamento = parcelamentoRepository.findById(parcela.getParcelamento()).orElseThrow(()-> new RegistroNaoLocalizadoException(Entities.PARCELAMENTO, ID));
        CadastroEntity cadastro = cadastroRepository.findById(parcelamento.getCadastro()).orElseThrow(()-> new RegistroNaoLocalizadoException(Entities.CADASTRO_ENTITY, ID));

        Double valor = valorBoleto == null || valorBoleto.equals(0.0)? parcela.getDetalhe().getValorAtual(): valorBoleto;
        dataVencimento = dataVencimento==null? parcela.getDetalhe().getDataVencimento() : dataVencimento;
        return gerarBoleto(cadastro, parcela, dataVencimento, valor);
    }
    private BoletoResponse gerarBoleto(CadastroEntity cadastro, ParcelaEntity parcela, LocalDate dataVencimento, Double valor){
        ParcelaBoleto boleto = parcela.getBoleto();
        if(boleto.isSolicitado() && boleto.getStatus()==ParcelaBoletoStatus.EMITIDO){
            BoletoResponse response = new BoletoResponse();
            return response;
        }
        parcela.getBoleto().setSolicitado(true);
        parcela.getBoleto().setStatus(ParcelaBoletoStatus.SOLICITADO);
        parcela.getDetalhe().setDataVencimento(dataVencimento);


        if(cadastro.getEmail()==null || cadastro.getCpfCnpj()==null || !Validation.cpfCnpj(cadastro.getCpfCnpj()))
            throw new CampoObrigatorioException("É necessário informar o CPF/CNPJ e e-mail no cadastro do cliente");

        Cadastro cad = new Cadastro();
        cad.setCpfCnpj(cad.getCpfCnpj());
        cad.setId(cadastro.getIntegracao()==null ? null : cadastro.getIntegracao().getAsaas());
        if(Validation.isEmpty(cad.getId())){
            cad.setDeleted(false);
            cad.setEmail(cadastro.getEmail());
            cad.setName(cadastro.getNomeFantasia());
            cad.setCpfCnpj(cadastro.getCpfCnpj());
            cad = integrador.cadastrar(cad);
            CadastroIntegracao integracao = new CadastroIntegracao();
            integracao.setAsaas(cad.getId());
            cadastro.setIntegracao(integracao);
            integrador.desativarNotificacoes(cadastro.getIntegracao().getAsaas());
            cadastroRepository.save(cadastro);
        }
        BoletoRequest boletoRequest = new BoletoRequest();
        boletoRequest.setDescription(parcela.getDescricao());
        boletoRequest.setValue(valor);
        boletoRequest.setDueDate(dataVencimento.toString());
        boletoRequest.setCustomer(cadastro.getIntegracao().getAsaas());
        boletoRequest.setExternalReference(parcela.getId().toString());

        BoletoResponse response = integrador.gerarBoleto(boletoRequest);
        if(response!=null){
            boleto.setStatus(ParcelaBoletoStatus.EMITIDO);
            boleto.setNumeroAutorizacao(response.getId());
            boleto.setUrlImpressao(response.getBankSlipUrl());
            parcelaRepository.save(parcela);
        }
        System.out.println(response);

        return response;
    }
    /*
    @Transactional
    public void compensar(ParcelaEntity parcela){

        BoletoResponse response = integrador.obterBoleto(parcela.getBoleto().getNumeroAutorizacao());
        ParcelamentoEntity parcelamento = parcelamentoRepository.findById(parcela.getParcelamento()).orElseThrow(()-> new RegistroNaoLocalizadoException(Entities.PARCELAMENTO,ID));

        PagamentoRequest lancamento = new PagamentoRequest();
        lancamento.setFormaPagamento(MeioPagamento.BOLETO);
        lancamento.setTipo(LancamentoTipo.RECEITA);
        lancamento.setCadastro(parcelamento.getPartes().getCadastro());
        LancamentoDetalheRequest detalhe = new LancamentoDetalheRequest();
        detalhe.setNumeroDocumento("asaas_"+response.getNossoNumero());
        detalhe.setDescricao( String.format("Comp.Aut.Bol Parc.:%d/%d", parcelamento.getId(), parcela.getId()) );
        detalhe.setValor(response.getNetValue());
        lancamento.setDetalhe(detalhe);

        EmpresaFormaPagamentoEntity conta = empresaContaMeioPagamentoRepository.findByEmpresaAndMeioPagamento(parcelamento.getPartes().getEmpresa(), MeioPagamento.BOLETO);
        Integer contaBanco = conta==null?null:conta.getConta();
        lancamento.setContaBancoEmpresa(contaBanco);
        lancamentoService.incluir(lancamento, null);

        ParcelaBoleto boleto = parcela.getBoleto();
        boleto.setDataCompensacao(response.getPaymentDate());
        boleto.setDataPagamento(response.getClientPaymentDate());
        boleto.setValorCompensado(response.getNetValue());
        boleto.setStatus(ParcelaBoletoStatus.PAGO);

        //atualiza o valor da parcela o total do parcelamento
        parcelamento.getNegociacao().setValorAmortizado(scale2(parcelamento.getNegociacao().getValorAmortizado() + response.getValue()).doubleValue());
        parcelamento.getNegociacao().setValorAtual(Math.abs(scale2(parcelamento.getNegociacao().getValorAtual() - response.getValue()).doubleValue()));
        parcelamentoRepository.save(parcelamento);

        parcela.getNegociacao().setValorAmortizado(scale2(parcela.getNegociacao().getValorAmortizado() + response.getValue()).doubleValue());
        parcela.getNegociacao().setValorAtual(Math.abs( scale2(parcela.getNegociacao().getValorAtual() - response.getValue()).doubleValue()));

        parcela.getQuitacao().setEfetuada(Validation.isZero(parcela.getNegociacao().getValorAtual()));
        parcela.getQuitacao().setData(parcela.getQuitacao().isEfetuada()?LocalDate.now():null);
        parcelaRepository.save(parcela);

        //etapa de gerar a linha de pagamento
        ParcelaPagamentoEntity pagamento = new ParcelaPagamentoEntity();
        pagamento.setData(LocalDate.now());
        pagamento.setCompetencia(RegistroData.periodo(pagamento.getData()));
        pagamento.setContaBanco(contaBanco);
        pagamento.setParcela(parcela.getId());
        pagamento.setParcelamento(parcelamento.getId());
        pagamento.setValorOrigianal(response.getValue());
        pagamento.setValor(response.getNetValue());
        pagamento.setMeioPagamento(MeioPagamento.BOLETO);
        pagamento.setUsuario(parcelamento.getPartes().getUsuario());
        pagamento.setNumeroAutorizacao(boleto.getNumeroAutorizacao());
        parcelaPagamentoRepository.save(pagamento);
    }
    */
}
