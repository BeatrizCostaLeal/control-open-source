package digytal.desktop.app.form.modulo.contrato;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.Date;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import digytal.desktop.app.context.Context;
import digytal.desktop.app.form.modulo.cadastro.cadastro.FrmLocalizaCadastro;
import digytal.desktop.app.form.modulo.cadastro.produto.FrmLocalizaProduto;
import digytal.desktop.app.form.modulo.financeiro.pagamento.FrmPagamento;
import digytal.desktop.app.form.modulo.financeiro.pagamento.FrmPagamentoAVista;
import digytal.desktop.app.form.modulo.financeiro.pagamento.FrmPagamentoBoleto;
import digytal.desktop.app.form.modulo.financeiro.pagamento.FrmPagamentoCartao;
import digytal.desktop.app.form.modulo.financeiro.pagamento.FrmPagamentoParcelado;
import digytal.desktop.app.model.modulo.cadastro.CadastroResponse;
import digytal.desktop.app.model.modulo.cadastro.produto.ProdutoResponse;
import digytal.desktop.app.model.modulo.contrato.ContratoItemRequest;
import digytal.desktop.app.model.modulo.contrato.ContratoRequest;
import digytal.desktop.app.model.modulo.contrato.ContratoTipo;
import digytal.desktop.app.model.modulo.financeiro.request.FormaPagamentoRequest;
import digytal.desktop.app.service.modulo.contrato.ContratoService;
import digytal.desktop.app.utils.Calculos;
import digytal.desktop.components.Formato;
import digytal.desktop.components.desktop.Formulario;
import digytal.desktop.components.desktop.ss.SSBotao;
import digytal.desktop.components.desktop.ss.SSCampoDataHora;
import digytal.desktop.components.desktop.ss.SSCampoNumero;
import digytal.desktop.components.desktop.ss.SSCampoTexto;
import digytal.desktop.components.desktop.ss.SSGrade;
import digytal.desktop.components.desktop.ss.SSMensagem;
import digytal.desktop.components.desktop.ss.evento.SSPesquisaEvento;
import digytal.desktop.components.desktop.ss.evento.SSPesquisaListener;
import digytal.desktop.components.desktop.ss.util.SSFormatador;
import digytal.desktop.util.utils.business.BusinessException;
import digytal.desktop.util.utils.http.Response;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FrmContratoVendaServicoModelo extends Formulario {
	private SSCampoDataHora cData = new SSCampoDataHora();
	private SSCampoTexto cCliente = new SSCampoTexto();
	private SSCampoTexto cNomeCliente = new SSCampoTexto();

	private SSCampoTexto cItem = new SSCampoTexto();
	private SSCampoTexto cNomeItem = new SSCampoTexto();
	private SSCampoNumero cValorUnitario = new SSCampoNumero();
	private SSCampoNumero cQuantidade = new SSCampoNumero();
	private SSCampoNumero cValorAplicado = new SSCampoNumero();
	private SSCampoNumero cSaldo = new SSCampoNumero();
	private SSBotao bAdd = new SSBotao();
	private SSGrade tabItens = new SSGrade();
	private SSGrade tPagamentos = new SSGrade();

	private SSBotao bIncluir = new SSBotao();

	private SSBotao bSalvar = new SSBotao();
	private SSBotao bFechar = new SSBotao();
	private final SSBotao bAVista = new SSBotao();

	private final JLabel lblNewLabel = new JLabel("R$ SUBTOTAL (=)");
	private final JLabel lblNewLabel_2 = new JLabel("R$ DESCONTO (-)");
	private final JLabel lblNewLabel_3 = new JLabel("R$ ACRÉSCIMO (+)");
	private final JLabel cSubtotal = new JLabel("R$");
	private final JLabel cDesconto = new JLabel("R$");
	private final JLabel cAcrescimo = new JLabel("R$");
	private final JLabel lblNewLabel_4 = new JLabel("TOTAL A PAGAR");
	private final JLabel cTotal = new JLabel("R$");

	private final JLabel lblNewLabel_3_1 = new JLabel("R$ RESTANTE (=)");
	private final JLabel cRestante = new JLabel("R$");
	private final SSBotao bCartao = new SSBotao();
	private final SSBotao bBoleto = new SSBotao();
	private final SSBotao bParcelado = new SSBotao();


	private Double descontoManual=0.0;
	private Double acrescimoPagamento=0.0;
	
	@Autowired
	private ContratoService service;
	private ContratoRequest contrato;
	private ContratoItemRequest item;
	private ContratoTipo tipo;
	public FrmContratoVendaServicoModelo() {
		setTitulo("Venda \\ Serviço");
		setDescricao("Registro de Vendas ou Prestação de Serviços");
		getConteudo().setLayout(new BorderLayout());
		JPanel pnlTopo = new JPanel();
		JPanel pnlInferior = new JPanel();

		JPanel pnlItem = new JPanel();
		pnlItem.setBorder(new TitledBorder(null, "Dados do Item", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JPanel pnlFinalizacao = new JPanel(new GridBagLayout());

		pnlFinalizacao.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Totais",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

		pnlInferior.setLayout(new BorderLayout());

		pnlTopo.setBorder(new TitledBorder(null, "Dados da Venda ou Serviço", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlTopo.setLayout(new GridBagLayout());

		pnlItem.setLayout(new GridBagLayout());

		GridBagConstraints gbc_cData = new GridBagConstraints();
		gbc_cData.fill = GridBagConstraints.HORIZONTAL;
		gbc_cData.insets = new Insets(3, 3, 3, 0);
		gbc_cData.anchor = GridBagConstraints.NORTHWEST;
		gbc_cData.gridx = 0;
		gbc_cData.gridy = 0;
		cData.setEditavel(false);
		cData.setRotulo("Data Venda");
		pnlTopo.add(cData, gbc_cData);

		GridBagConstraints gbc_cCliente = new GridBagConstraints();
		gbc_cCliente.fill = GridBagConstraints.HORIZONTAL;
		gbc_cCliente.insets = new Insets(3, 3, 3, 0);
		gbc_cCliente.anchor = GridBagConstraints.NORTHWEST;
		gbc_cCliente.gridx = 1;
		gbc_cCliente.gridy = 0;
		cCliente.setPesquisa(true);
		cCliente.setRotulo("Cliente");
		pnlTopo.add(cCliente, gbc_cCliente);

		GridBagConstraints gbc_cNomeCliente = new GridBagConstraints();
		gbc_cNomeCliente.weighty = 1.0;
		gbc_cNomeCliente.weightx = 1.0;
		gbc_cNomeCliente.fill = GridBagConstraints.HORIZONTAL;
		gbc_cNomeCliente.insets = new Insets(3, 3, 3, 3);
		gbc_cNomeCliente.anchor = GridBagConstraints.NORTHWEST;
		gbc_cNomeCliente.gridx = 2;
		gbc_cNomeCliente.gridy = 0;
		cNomeCliente.setComponenteNegrito(true);
		cNomeCliente.setComponenteFonte(new Font("Tahoma", Font.BOLD, 11));
		cNomeCliente.setComponenteCorFonte(new Color(0, 0, 139));
		cNomeCliente.setForeground(new Color(0, 0, 0));
		cNomeCliente.setEditavel(false);
		cNomeCliente.setColunas(30);

		cNomeCliente.setRotulo("CPF\\CNPJ - Nome \\ Razão Social");
		pnlTopo.add(cNomeCliente, gbc_cNomeCliente);

		GridBagConstraints gbc_cItem = new GridBagConstraints();
		gbc_cItem.fill = GridBagConstraints.HORIZONTAL;
		gbc_cItem.insets = new Insets(3, 3, 3, 0);
		gbc_cItem.anchor = GridBagConstraints.NORTHWEST;
		gbc_cItem.gridx = 0;
		gbc_cItem.gridy = 0;
		cItem.setPesquisa(true);
		cItem.setRotulo("ID (sku)");
		pnlItem.add(cItem, gbc_cItem);

		GridBagConstraints gbc_cNomeItem = new GridBagConstraints();
		gbc_cNomeItem.weightx = 1.0;
		gbc_cNomeItem.fill = GridBagConstraints.BOTH;
		gbc_cNomeItem.insets = new Insets(3, 3, 3, 0);
		gbc_cNomeItem.anchor = GridBagConstraints.NORTHWEST;
		gbc_cNomeItem.gridx = 1;
		gbc_cNomeItem.gridy = 0;
		cNomeItem.setComponenteNegrito(true);
		cNomeItem.setEditavel(false);
		cNomeItem.setComponenteFonte(new Font("Tahoma", Font.BOLD, 11));
		cNomeItem.setComponenteCorFonte(new Color(128, 0, 0));
		cNomeItem.setColunas(20);
		cNomeItem.setRotulo("Nome Produto");
		pnlItem.add(cNomeItem, gbc_cNomeItem);

		GridBagConstraints gbc_cSaldo = new GridBagConstraints();
		gbc_cSaldo.fill = GridBagConstraints.HORIZONTAL;
		gbc_cSaldo.insets = new Insets(3, 3, 3, 0);
		gbc_cSaldo.anchor = GridBagConstraints.NORTHWEST;
		gbc_cSaldo.gridx = 2;
		gbc_cSaldo.gridy = 0;
		cSaldo.setRotulo("Saldo Atual");
		pnlItem.add(cSaldo, gbc_cSaldo);

		GridBagConstraints gbc_cValorUnitario = new GridBagConstraints();
		gbc_cValorUnitario.fill = GridBagConstraints.HORIZONTAL;
		gbc_cValorUnitario.insets = new Insets(3, 3, 3, 0);
		gbc_cValorUnitario.anchor = GridBagConstraints.NORTHWEST;
		gbc_cValorUnitario.gridx = 3;
		gbc_cValorUnitario.gridy = 0;
		cValorUnitario.setColunas(6);
		cValorUnitario.setRotulo("R$ Unitário");
		pnlItem.add(cValorUnitario, gbc_cValorUnitario);

		GridBagConstraints gbc_cQuantidade = new GridBagConstraints();
		gbc_cQuantidade.fill = GridBagConstraints.HORIZONTAL;
		gbc_cQuantidade.insets = new Insets(3, 3, 3, 0);
		gbc_cQuantidade.anchor = GridBagConstraints.NORTHWEST;
		gbc_cQuantidade.gridx = 4;
		gbc_cQuantidade.gridy = 0;
		
		cQuantidade.setRotulo("Quantidade");
		pnlItem.add(cQuantidade, gbc_cQuantidade);

		GridBagConstraints gbc_cValorAplicado = new GridBagConstraints();
		gbc_cValorAplicado.fill = GridBagConstraints.HORIZONTAL;
		gbc_cValorAplicado.insets = new Insets(3, 3, 3, 3);
		gbc_cValorAplicado.anchor = GridBagConstraints.NORTHWEST;
		gbc_cValorAplicado.gridx = 5;
		gbc_cValorAplicado.gridy = 0;
		cValorAplicado.setColunas(8);
		cValorAplicado.setRotulo("R$ Valor Total");
		pnlItem.add(cValorAplicado, gbc_cValorAplicado);

		GridBagConstraints gbc_bAdd = new GridBagConstraints();
		gbc_bAdd.fill = GridBagConstraints.HORIZONTAL;
		gbc_bAdd.insets = new Insets(0, 3, 5, 5);
		gbc_bAdd.anchor = GridBagConstraints.SOUTHEAST;
		gbc_bAdd.gridx = 6;
		gbc_bAdd.gridy = 0;
		pnlItem.add(bAdd, gbc_bAdd);

		GridBagConstraints gbc_bIncluir = new GridBagConstraints();
		gbc_bIncluir.fill = GridBagConstraints.HORIZONTAL;
		gbc_bIncluir.insets = new Insets(3, 3, 5, 3);
		gbc_bIncluir.anchor = GridBagConstraints.SOUTHEAST;
		gbc_bIncluir.gridx = 6;
		gbc_bIncluir.gridy = 0;
		// pnlItem.add(bIncluir, gbc_bIncluir);

		

		tabItens.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		tabItens.adicionarColuna(0, "Cód.(SKU)", "produto");
		tabItens.adicionarColuna(1, "Nome do Produto \\ Serviço", "descricao");
		tabItens.adicionarColuna(2, "Und", "und");
		tabItens.adicionarColuna(3, "R$ Preço", "preco");
		tabItens.adicionarColuna(4, "R$ Unit.", "valorUnitario");
		tabItens.adicionarColuna(5, "Quantd.", "quantidade");
		tabItens.adicionarColuna(6, "R$ Total", "valorAplicado");
		tabItens.adicionarColuna(7, "R$ +/-", "valorVariacao");
		
		tabItens.definirLarguraColunas(75, 280, 45, 65, 75, 60,60,75);
		tabItens.getModeloColuna().setFormato(3, Formato.MOEDA);
		tabItens.getModeloColuna().setFormato(4, Formato.MOEDA);
		tabItens.getModeloColuna().setFormato(5, Formato.MOEDA);
		tabItens.getModeloColuna().setFormato(6, Formato.MOEDA);
		tabItens.getModeloColuna().setFormato(7, Formato.MOEDA);
		
		tabItens.getModeloColuna().definirPositivoNegativo(7);

		getConteudo().add(pnlTopo, BorderLayout.NORTH);
		getConteudo().add(pnlInferior, BorderLayout.SOUTH);
		
		pnlInferior.add(pnlItem, BorderLayout.NORTH);
		pnlInferior.add(pnlFinalizacao, BorderLayout.CENTER);

		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel.insets = new Insets(5, 5, 0, 2);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 0;
		pnlFinalizacao.add(lblNewLabel, gbc_lblNewLabel);

		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblNewLabel_4.gridheight = 2;
		gbc_lblNewLabel_4.weightx = 0.5;
		gbc_lblNewLabel_4.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel_4.insets = new Insets(5, 5, 5, 5);
		gbc_lblNewLabel_4.gridx = 3;
		gbc_lblNewLabel_4.gridy = 0;
		lblNewLabel_4.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNewLabel_4.setHorizontalAlignment(SwingConstants.RIGHT);
		pnlFinalizacao.add(lblNewLabel_4, gbc_lblNewLabel_4);

		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNewLabel_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel_2.insets = new Insets(5, 5, 0, 2);
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 1;
		pnlFinalizacao.add(lblNewLabel_2, gbc_lblNewLabel_2);

		GridBagConstraints gbc_cDesconto = new GridBagConstraints();
		gbc_cDesconto.anchor = GridBagConstraints.NORTHEAST;
		gbc_cDesconto.insets = new Insets(5, 5, 0, 5);
		gbc_cDesconto.gridx = 2;
		gbc_cDesconto.gridy = 1;
		cDesconto.setForeground(new Color(178, 34, 34));
		cDesconto.setHorizontalAlignment(SwingConstants.RIGHT);
		cDesconto.setFont(new Font("Tahoma", Font.BOLD, 11));
		cDesconto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		pnlFinalizacao.add(cDesconto, gbc_cDesconto);

		GridBagConstraints gbc_bCartao = new GridBagConstraints();
		gbc_bCartao.fill = GridBagConstraints.HORIZONTAL;
		gbc_bCartao.insets = new Insets(5, 5, 2, 5);
		gbc_bCartao.gridx = 4;
		gbc_bCartao.gridy = 1;
		bCartao.setText("Cartão");
		bCartao.setIcone("card");
		pnlFinalizacao.add(bCartao, gbc_bCartao);

		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(5, 5, 0, 2);
		gbc_lblNewLabel_3.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNewLabel_3.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel_3.gridx = 1;
		gbc_lblNewLabel_3.gridy = 2;
		lblNewLabel_3.setFont(new Font("Tahoma", Font.PLAIN, 11));
		pnlFinalizacao.add(lblNewLabel_3, gbc_lblNewLabel_3);

		GridBagConstraints gbc_cTotal = new GridBagConstraints();
		gbc_cTotal.fill = GridBagConstraints.HORIZONTAL;
		gbc_cTotal.gridheight = 3;
		gbc_cTotal.anchor = GridBagConstraints.NORTHEAST;
		gbc_cTotal.insets = new Insets(10, 5, 10, 5);
		gbc_cTotal.gridx = 3;
		gbc_cTotal.gridy = 1;
		cTotal.setForeground(new Color(0, 0, 255));
		cTotal.setFont(new Font("Tahoma", Font.BOLD, 17));
		cTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		pnlFinalizacao.add(cTotal, gbc_cTotal);

		GridBagConstraints gbc_cSubtotal = new GridBagConstraints();
		gbc_cSubtotal.insets = new Insets(5, 5, 0, 5);
		gbc_cSubtotal.anchor = GridBagConstraints.NORTHEAST;
		gbc_cSubtotal.fill = GridBagConstraints.HORIZONTAL;
		gbc_cSubtotal.gridx = 2;
		gbc_cSubtotal.gridy = 0;
		cSubtotal.setHorizontalAlignment(SwingConstants.RIGHT);
		cSubtotal.setFont(new Font("Tahoma", Font.BOLD, 11));
		pnlFinalizacao.add(cSubtotal, gbc_cSubtotal);

		GridBagConstraints gbc_bBoleto = new GridBagConstraints();
		gbc_bBoleto.fill = GridBagConstraints.HORIZONTAL;
		gbc_bBoleto.insets = new Insets(5, 5, 2, 5);
		gbc_bBoleto.gridx = 4;
		gbc_bBoleto.gridy = 2;
		bBoleto.setText("Boleto");
		bBoleto.setIcone("barcode");
		pnlFinalizacao.add(bBoleto, gbc_bBoleto);

		GridBagConstraints gbc_lblNewLabel_3_1 = new GridBagConstraints();
		gbc_lblNewLabel_3_1.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNewLabel_3_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel_3_1.insets = new Insets(5, 5, 5, 2);
		gbc_lblNewLabel_3_1.gridx = 1;
		gbc_lblNewLabel_3_1.gridy = 3;
		lblNewLabel_3_1.setFont(new Font("Tahoma", Font.PLAIN, 11));
		pnlFinalizacao.add(lblNewLabel_3_1, gbc_lblNewLabel_3_1);

		GridBagConstraints gbc_cAcrescimo = new GridBagConstraints();
		gbc_cAcrescimo.insets = new Insets(5, 5, 0, 5);
		gbc_cAcrescimo.anchor = GridBagConstraints.NORTHEAST;
		gbc_cAcrescimo.gridx = 2;
		gbc_cAcrescimo.gridy = 2;
		cAcrescimo.setForeground(new Color(0, 51, 255));
		cAcrescimo.setHorizontalAlignment(SwingConstants.RIGHT);
		cAcrescimo.setFont(new Font("Tahoma", Font.BOLD, 11));
		pnlFinalizacao.add(cAcrescimo, gbc_cAcrescimo);

		GridBagConstraints gbc_cRestante = new GridBagConstraints();
		gbc_cRestante.anchor = GridBagConstraints.NORTHEAST;
		gbc_cRestante.insets = new Insets(5, 5, 5, 5);
		gbc_cRestante.gridx = 2;
		gbc_cRestante.gridy = 3;
		cRestante.setForeground(new Color(0, 0, 0));
		cRestante.setHorizontalAlignment(SwingConstants.RIGHT);
		cRestante.setFont(new Font("Tahoma", Font.BOLD, 11));
		pnlFinalizacao.add(cRestante, gbc_cRestante);

		GridBagConstraints gbc_bAVista = new GridBagConstraints();
		gbc_bAVista.fill = GridBagConstraints.HORIZONTAL;
		gbc_bAVista.insets = new Insets(5, 5, 2, 5);
		gbc_bAVista.anchor = GridBagConstraints.NORTHEAST;
		gbc_bAVista.gridx = 4;
		gbc_bAVista.gridy = 0;

		pnlFinalizacao.add(bAVista, gbc_bAVista);

		GridBagConstraints gbc_bParcelado = new GridBagConstraints();
		gbc_bParcelado.insets = new Insets(5, 5, 5, 5);
		gbc_bParcelado.fill = GridBagConstraints.HORIZONTAL;
		gbc_bParcelado.gridx = 4;
		gbc_bParcelado.gridy = 3;
		pnlFinalizacao.add(bParcelado, gbc_bParcelado);
		
		JScrollPane scroll = new JScrollPane();
		//scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setPreferredSize(new Dimension(750, 150));
		scroll.setViewportView(tabItens);
		getConteudo().add(scroll, BorderLayout.CENTER);
		
		JScrollPane scrollPagamentos = new JScrollPane();
		//scrollPagamentos.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPagamentos.setPreferredSize(new Dimension(0, 100));
		scrollPagamentos.setViewportView(tPagamentos);

		
		GridBagConstraints gbc_tPagamentos = new GridBagConstraints();
		gbc_tPagamentos.weighty = 1.0;
		gbc_tPagamentos.weightx = 2.0;
		gbc_tPagamentos.gridheight = 4;
		gbc_tPagamentos.insets = new Insets(5, 5, 5, 5);
		gbc_tPagamentos.fill = GridBagConstraints.BOTH;
		gbc_tPagamentos.gridx = 0;
		gbc_tPagamentos.gridy = 0;
		pnlFinalizacao.add(scrollPagamentos,gbc_tPagamentos );

		getRodape().add(bSalvar);
		getRodape().add(bFechar);
		
		definirValorPadrao();
		definirEventos();
	}

	private void definirValorPadrao() {
		//0800 606 8686
		bParcelado.setText("Carnê");
		bParcelado.setIcone("date");
		tPagamentos.adicionarColuna(0, "Pagamento", "meioPagamento");
		tPagamentos.adicionarColuna(1, "R$ Valor", "valorOriginal");
		tPagamentos.adicionarColuna(2, "R$ (+/-)", "valorAcrescimo");
		tPagamentos.adicionarColuna(3, "Pcr", "parcelamento.numeroParcelas");
		tPagamentos.adicionarColuna(4, "Tx%", "taxaPagamento");
		
		tPagamentos.definirLarguraColunas(80, 75,60,30,40);
		tPagamentos.getModeloColuna().setFormato(1, Formato.MOEDA);
		tPagamentos.getModeloColuna().setFormato(2, Formato.MOEDA);
		tPagamentos.getModeloColuna().setFormato(4, Formato.TAXA);
		
		
		cData.setDataHora(new Date());
		cNomeCliente.somenteLeitura();
		cNomeItem.somenteLeitura();
		cSaldo.setEditavel(false);
		cCliente.setEditavel(false, true);
		bSalvar.setText("Concluir");
		bSalvar.setIcone("ok");

		bFechar.setText("Fechar");
		bIncluir.setText("Incluir");
		cValorUnitario.setFormato(Formato.MOEDA);
		cQuantidade.setFormato(Formato.MOEDA);
		cSaldo.setFormato(Formato.MOEDA);
		cValorAplicado.setFormato(Formato.MOEDA);

		cValorUnitario.setMascara(Formato.MOEDA);
		cQuantidade.setMascara(Formato.MOEDA);
		cSaldo.setMascara(Formato.MOEDA);
		cValorAplicado.setMascara(Formato.MOEDA);
		cValorAplicado.setEditavel(false);

		bAVista.setText("À Vista");
		bAVista.setIcone("money");
		
		bAdd.setText("OK");
		bAdd.setIcone("add");

		cQuantidade.setSelecionarAoEntrar(true);
		cValorUnitario.setSelecionarAoEntrar(true);
		cValorAplicado.setSelecionarAoEntrar(true);
		cNomeItem.setColunas(25);
	}
	public void iniciar(ContratoTipo tipo) {
		cNomeCliente.setText("**CLIENTE NAO INFORMADO**");
		this.tipo = tipo;
		this.contrato = new ContratoRequest();
	}
	private void definirEventos() {
		cDesconto.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				aplicarDesconto();
			}
		});
		
		cCliente.addPesquisaListener(new SSPesquisaListener() {
			@Override
			public void pesquisaListener(SSPesquisaEvento evento) {
				localizarCliente();
			}
		});
		cItem.addPesquisaListener(new SSPesquisaListener() {
			@Override
			public void pesquisaListener(SSPesquisaEvento evento) {
				localizarProduto();
			}
		});

		cItem.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					localizarProduto();
				}
			}

		});

		bSalvar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				concluir();
			}
		});

		bFechar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fechar();
			}
		});

		cQuantidade.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {
				calcularValorItemAplicao();
				// cValorAplicado.requestFocus();
			}
		});
		cValorUnitario.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {
				calcularValorItemAplicao();
				// cValorAplicado.requestFocus();
			}
		});

		cItem.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {
				// exibirProdutoSku(cItem.getText());
				// cValorUnitario.requestFocus();
			}
		});
		
		bAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addItem();
			}
		});

		bAVista.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				realizarPagamento(CondicaoPagamento.A);
			}
		});
		
		bCartao.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				realizarPagamento(CondicaoPagamento.DC);
			}
		});
		bBoleto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				realizarPagamento(CondicaoPagamento.B);
			}
		});
		bParcelado.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				realizarPagamento(CondicaoPagamento.P);
			}
		});
		tPagamentos.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent mouseEvent) {
		        if (mouseEvent.getClickCount() == 2) {
		        	removerPagamento();
		        }
		    }
		});
		tabItens.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent mouseEvent) {
		        if (mouseEvent.getClickCount() == 2) {
		        	removerItem();
		        }
		    }
		});

	}
	private void aplicarDesconto() {
		/*
		if(venda.getPagamentos()!=null && venda.getPagamentos().size() >0) {
			SSMensagem.avisa("Só é permitido Desconto Manual antes do rateiro de pagamentos");
			return;
		}
		FrmPagamento frm = Context.getBean(FrmPagamentoDesconto.class);
		frm.iniciar(venda.getValorDesconto()-descontoManual);
		descontoManual = (Double) Formulario.dialogo(frm);
		totalizar();
		*/
	}
	private void removerItem() {
		ContratoItemRequest item = (ContratoItemRequest) tabItens.getLinhaSelecionada();
		
		if (item!=null && SSMensagem.pergunta("Deseja remover o item " + item.getDescricao() + " da venda")) {
			contrato.getItens().remove(item);
			tabItens.setValue(contrato.getItens());
			totalizar();
		}

	}
	
	private void concluir() {
		try {
			contrato.setCadastro(1);
			contrato.setData(LocalDate.now());
			contrato.setValorDescontoManual(descontoManual);
			contrato.setDescricao("Venda\\Servico");
			Response<Integer> response = service.gerarContratoVenda(contrato);

			SSMensagem.informa(response.getStatus().getMessage());

			this.fechar();
			/*
			if (SSMensagem.pergunta("Confirma concluir este lançamento")) {
				if (contrato.getCadastro() == null) {
					if(SSMensagem.pergunta("Confirmar vender para o cliente **CLIENTE NAO INFORMADO**?")) {
						contrato.setCadastro(1);
					}else
						return;
				}
				
			}
			*/
		} catch (BusinessException ne) {
			SSMensagem.avisa(ne.getErrorCode(), ne.getMessage());
		} catch (Exception ex) {
			//LOGGER.error("ERRO AO REQUISITAR API", ex);
			SSMensagem.erro("Erro ao tentar executar a operação");
		}
		
	}

	private void localizarProduto() {
		
		bAdd.setEnabled(true);
		FrmLocalizaProduto frm = Context.getBean(FrmLocalizaProduto.class);
		frm.exibir(false);
		ProdutoResponse produto = (ProdutoResponse) Formulario.dialogo(frm);
		
		ContratoItemRequest itemLocalizado = contrato.getItens().stream()
	            .filter(e -> e.getProduto().equals(produto.getId()))
	            .findFirst()
	            .orElse(null);
		if(itemLocalizado!=null) {
			SSMensagem.avisa("Já existe o produto " + produto.getNome() + " na lista");
			bAdd.setEnabled(false);
			return;
		}
		exibirProduto(produto);
		
	}

	private void addItem() {
		item.setValorUnitario(cValorUnitario.getDouble());
		item.setQuantidade(cQuantidade.getDouble());
		item.setValorAplicado(Calculos.multiplicar(item.getValorUnitario(), item.getQuantidade()));
		Double variacao = Calculos.multiplicar(item.getPreco(), item.getQuantidade());
		variacao = Calculos.subtrair(item.getValorAplicado(),variacao) ;
		
		item.setValorVariacao(variacao);
		contrato.getItens().add(item);
		tabItens.setValue(contrato.getItens());
		exibirProduto(null);
		totalizar();
		
	}

	private void exibirProduto(ProdutoResponse produto) {
		cNomeItem.setText("");
		cItem.setText("");
		cSaldo.setValue(0.0);
		cQuantidade.setValue(1.0);
		cValorUnitario.setValue(0.0);
		cValorAplicado.setValue(0.0);
		cItem.requestFocus();
		if (produto != null) {
			item = new ContratoItemRequest();
			item.setProduto(produto.getId());
			item.setPreco(produto.getValor());
			item.setDescricao(produto.getNome());
			item.setUnd(produto.getUnidadeMedida().getAbreviacao());
			item.setValorUnitario(produto.getValor());
			exibirItem();
		}
		habilitarCampoItem(produto != null);
		
	}

	private void exibirItem() {
		
		cItem.setText(item.getProduto().toString());
		cNomeItem.setText(item.getDescricao());
		cSaldo.setValue(0.0);
		cValorUnitario.setValue(item.getValorUnitario());
		cValorAplicado.setValue(item.getValorUnitario());
		cValorUnitario.requestFocus();
		
	}

	private void localizarCliente() {
		cCliente.setText("");
		cNomeCliente.setText("**CLIENTE NAO INFORMADO**");
		
		FrmLocalizaCadastro frm = Context.getBean(FrmLocalizaCadastro.class);
		CadastroResponse cliente = (CadastroResponse) Formulario.dialogo(frm);

		if (cliente != null) {
			cCliente.setText(cliente.getId().toString());
			String cpfCnpj = SSFormatador.formatarCpfCnpj(cliente.getCpfCnpj());
			cNomeCliente.setText(cpfCnpj + " - " + cliente.getNomeFantasia());
			cItem.requestFocus();
			//venda.setCadastro(cliente.getId());
		}
	}

	private void realizarPagamento(CondicaoPagamento condicao) {
		
		FrmPagamento frm = Context.getBean(FrmPagamentoAVista.class);
		if(CondicaoPagamento.DC == condicao)
			frm = Context.getBean(FrmPagamentoCartao.class);
		else if(CondicaoPagamento.B == condicao)
			frm = Context.getBean(FrmPagamentoBoleto.class);
		else if(CondicaoPagamento.P == condicao)
			frm = Context.getBean(FrmPagamentoParcelado.class);
		
		frm.iniciar(getRestante());
		FormaPagamentoRequest pagamento = (FormaPagamentoRequest) Formulario.dialogo(frm);
		
		if(pagamento!=null) {
			if(adicionarPagamento(pagamento.getValorOriginal())) {
				contrato.getFormasPagamento().add(pagamento);
				acrescimoPagamento = acrescimoPagamento + pagamento.getValorAcrescimo();
				tPagamentos.setValue(contrato.getFormasPagamento());
				calcularValorAcrescimo();
			}else {
				SSMensagem.avisa("O valor informado ultrapassa o valor restante");
				return;
			}
		}
		habilitarPagamentos(calcularValorRestante());
		totalizar();
		
	}	
	

	private void totalizar() {
		
		cDesconto.setText(SSFormatador.formatar(0.0));
		cAcrescimo.setText(SSFormatador.formatar(0.0));

		if (contrato == null) {
			cSubtotal.setText(SSFormatador.formatar(0.0));
			cTotal.setText(SSFormatador.formatar(0.0));
			cRestante.setText(SSFormatador.formatar(0.0));
			habilitarPagamentos(false);
		} else {
			Double valorAplicado = contrato.getItens().stream()
					.collect(Collectors.summingDouble(o -> o.getValorAplicado()));
			Double valorPrevisto = contrato.getItens().stream()
					.collect(Collectors.summingDouble(o -> o.getPreco() * o.getQuantidade()));
			
			habilitarPagamentos(calcularValorRestante());
			calcularValorDesconto();
			calcularValorAcrescimo();
			
			contrato.setValorAplicado(valorAplicado);
			
			cSubtotal.setText(SSFormatador.formatar(valorPrevisto));
			cTotal.setText("R$ " + SSFormatador.formatar(valorAplicado + acrescimoPagamento - descontoManual));	
		}
		
	}
	private void calcularValorAcrescimo() {
		/*
		Double valor = venda.getItens().stream().filter(e -> Double.compare(e.getValorResidualNegociacao(),0.0) > 0)
				.collect(Collectors.summingDouble(o -> o.getValorResidualNegociacao()));
		
		valor = Math.abs(valor==null ? 0.0 : valor);
		venda.setValorAcrescimo(valor + acrescimoPagamento);
		cAcrescimo.setText(SSFormatador.formatar(venda.getValorAcrescimo()));
		*/
	}
	private void habilitarCampoItem(boolean habilita) {
		cQuantidade.setEditavel(habilita);
		cValorUnitario.setEditavel(habilita);
		bAdd.setEnabled(habilita);
	}
	private void removerPagamento() {
		/*
		if (SSMensagem.pergunta("Deseja remover o pagamento selecionado")) {
			ContratoPagamentoRequest pagto = (ContratoPagamentoRequest) tPagamentos.getLinhaSelecionada();
			venda.getPagamentos().remove(pagto);
			acrescimoPagamento = acrescimoPagamento - pagto.getValorAcrescimo();
			tPagamentos.setValue(venda.getPagamentos());
			totalizar();
		}
		*/
	}

	private void habilitarPagamentos(boolean habilitado) {
		bAVista.setEnabled(habilitado);
		bCartao.setEnabled(habilitado);
		bBoleto.setEnabled(habilitado);
		bParcelado.setEnabled(habilitado);
	}

	private void calcularValorItemAplicao() {
		//item.setQuantidade(cQuantidade.getDouble());
		//item.setValorAplicado(item.getQuantidade() * cValorUnitario.getDouble());
		//cValorAplicado.setValue(item.getValorAplicado());
	}

	private boolean calcularValorRestante() {
		Double restante = getRestante();
		cRestante.setText(SSFormatador.formatar(restante));
		return restante.compareTo(Double.valueOf(0.0)) == 1;
	}

	private Double getRestante() {
		Double valorAplicado = contrato.getItens().stream().collect(Collectors.summingDouble(o -> o.getValorAplicado()));
		Double valorPago = contrato.getFormasPagamento().stream().collect(Collectors.summingDouble(o -> o.getValorPago()));
		return valorAplicado + acrescimoPagamento - descontoManual -  valorPago;
	}
	private boolean adicionarPagamento(Double novoValor) {
		Double restante = getRestante();
		restante = restante - novoValor;
		return Double.compare(restante,0.0) >=0;
	}
	
	private void calcularValorDesconto() {
		/*
		Double valor = venda.getItens().stream().filter(e -> Double.compare(e.getValorResidualNegociacao(),0.0) < 0)
				.collect(Collectors.summingDouble(o -> o.getValorResidualNegociacao()));
		
		valor = Math.abs(valor==null ? 0.0 : valor);
		venda.setValorDesconto(valor + descontoManual);
		cDesconto.setText(SSFormatador.formatar(venda.getValorDesconto()));
		*/
	}
	
	
	private enum CondicaoPagamento{
		A,
		DC,
		B,
		P
	}
	
}
