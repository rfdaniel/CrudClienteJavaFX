package controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import factory.JPAFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Cidade;
import model.Cliente;
import model.Telefone;
import repository.CidadeRepository;
import repository.ClienteRepository;

public class ClienteController extends Controller<Cliente> implements Initializable {

	private Cliente cliente;
	
	private Cidade cidade;

	@FXML
	private TabPane tpAbas;

	@FXML
	private TextField tfNome, tfCpf, tfEndereco, tfEmail;

	@FXML
	private DatePicker dpAniversario;

	@FXML
	private Button btLimpar, btIncluir, btExcluir, btAlterar;

	@FXML
	private TableView<Cliente> tvClientes;

	@FXML
	private TableColumn<Cliente, Integer> tcIdCliente;

	@FXML
	private TableColumn<Cliente, String> tcCpfCliente;

	@FXML
	private TableColumn<Cliente, String> tcNomeCliente;

	@FXML
	private TableColumn<Cliente, String> tcEnderecoCliente;
	
    @FXML
    private ComboBox<Cidade> cbCidadeNatal;

	@FXML
	private TableColumn<Cliente, String> tcEmailCliente;

	@FXML
	private TableColumn<Cliente, LocalDate> dataAniversario;

	@FXML
	private TextField tfDdd;

	@FXML
	private TextField tfNumeroTelefone;

	@FXML
	private Button btIncluirTelefone;

	@FXML
	private TableView<Telefone> tbTelefone;

	@FXML
	private TableColumn<Telefone, String> tcDdd;

	@FXML
	private TableColumn<Telefone, String> tcTelefone;

	@FXML
	private TextField tfPesquisar;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// SETANDO O FOCUS NO TEXT FIELD NOME
		tfNome.requestFocus();

		// CONFIGURANDO AS COLUNAS DAS TABELAS CONFORME OS ATRIBUTOS DA CLASSE CLIENTE
		tcIdCliente.setCellValueFactory(new PropertyValueFactory<>("id"));
		tcCpfCliente.setCellValueFactory(new PropertyValueFactory<>("cpf"));
		tcNomeCliente.setCellValueFactory(new PropertyValueFactory<>("nome"));
		tcEnderecoCliente.setCellValueFactory(new PropertyValueFactory<>("endereco"));
		tcEmailCliente.setCellValueFactory(new PropertyValueFactory<>("email"));
		dataAniversario.setCellValueFactory(new PropertyValueFactory<>("dataAniversario"));

		// CONFIGURANDO AS COLUNAS DAS TABELAS CONFORME OS ATRIBUTOS DA CLASSE TELEFONE
		tcDdd.setCellValueFactory(new PropertyValueFactory<>("codigoArea"));
		tcTelefone.setCellValueFactory(new PropertyValueFactory<>("numero"));

		// ATUALIZAR BOTÕES
		atualizarBotoes();
		
		carregarComboBox();
	}

	@FXML
	void handleAdicionarTelefone(ActionEvent event) {
		Telefone telefone = new Telefone();
		telefone.setCodigoArea(tfDdd.getText());
		telefone.setNumero(tfNumeroTelefone.getText());
		telefone.setCliente(cliente);

		if (getCliente().getListaTelefone() == null)
			getCliente().setListaTelefone(new ArrayList<Telefone>());

		getCliente().getListaTelefone().add(telefone);

		tbTelefone.setItems(FXCollections.observableList(getCliente().getListaTelefone()));

		tfDdd.clear();
		tfNumeroTelefone.clear();
		tfDdd.requestFocus();
	}

	@FXML
	void handlePesquisar(ActionEvent event) {
		ClienteRepository repository = new ClienteRepository(JPAFactory.getEntityManager());
		List<Cliente> lista = repository.getClientes(tfPesquisar.getText());

		if (lista.isEmpty()) {
			Alert alerta = new Alert(AlertType.INFORMATION);
			alerta.setTitle("Informação:");
			alerta.setHeaderText(null);
			alerta.setContentText("A consulta não retornou dados!");
			alerta.show();
		}

		tvClientes.setItems(FXCollections.observableList(lista));
	}

	@FXML
	void handleOnMouseClicked(MouseEvent event) {

		// VERIFICANDO SE Ã‰ O BOTÃƒO PRINCIPAL QUE FOI CLIADO
		if (event.getButton().equals(MouseButton.PRIMARY)) {
			// VERIFICANDO SE A QUANTIDADE DE CLIQUES NO BOTÃƒO PRIMÃ�RIO Ã‰ IGUAL A 2
			if (event.getClickCount() == 2) {

				cliente = tvClientes.getSelectionModel().getSelectedItem();

				tfCpf.setText(getCliente().getCpf());
				tfNome.setText(getCliente().getNome());
				tfEndereco.setText(getCliente().getEndereco());
				tfEmail.setText(getCliente().getEmail());
				dpAniversario.setValue(getCliente().getDataAniversario());

				// PREENCHER DADOS DA TABELA TELEFONE
				tbTelefone.setItems(FXCollections.observableList(getCliente().getListaTelefone()));

				// SELECIONANDO A PRIMEIRA ABA
				tpAbas.getSelectionModel().select(0);

				// SETANDO O FOCUS NO NOME
				tfNome.requestFocus();

				atualizarBotoes();
			}
		}
	}

	@FXML
	void handleAlterar(ActionEvent event) {

		getCliente().setCpf(tfCpf.getText());
		getCliente().setNome(tfNome.getText());
		getCliente().setEndereco(tfEndereco.getText());
		getCliente().setEmail(tfEmail.getText());
		getCliente().setDataAniversario(dpAniversario.getValue());

		super.save(getCliente());

		handleLimpar(event);
	}

	@FXML
	void handleExcluir(ActionEvent event) {

		// MENSAGEM DE ALERTA PARA O USUÃ�RIO CONFIRMAR UMA EXCLUSÃƒO
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmação");
		alert.setHeaderText("Está operação excluirá todas as informações selecionadas da base de dados.");
		alert.setContentText("Deseja realmente excluir?");
		// Capturar as resposta do usuÃ¡rio sobre a mensagem de confirmaÃ§Ã£o
		Optional<ButtonType> resposta = alert.showAndWait();
		if (resposta.get().equals(ButtonType.OK)) {
			super.remove(getCliente());
			handleLimpar(event);
		} else if (resposta.get().equals(ButtonType.CANCEL)) {

		}
	}

	@FXML
	void handleIncluir(ActionEvent event) {
		getCliente().setCpf(tfCpf.getText());
		getCliente().setNome(tfNome.getText());
		getCliente().setEndereco(tfEndereco.getText());
		getCliente().setEmail(tfEmail.getText());
		getCliente().setDataAniversario(dpAniversario.getValue());
		getCliente().setCidadNatal(cbCidadeNatal.getValue());

		super.save(getCliente());

		handleLimpar(event);
	}

	@FXML
	void handleLimpar(ActionEvent event) {
		tfCpf.setText("");
		tfNome.setText("");
		tfEmail.setText("");
		tfEndereco.setText("");
		dpAniversario.setValue(null);

		// LIMPANDO AS INFORMAÃ‡Ã•ES DO CLIENTE
		cliente = null;

		// LIMPAR AS TABELAS
		tvClientes.getItems().clear();
		tbTelefone.getItems().clear();

		tfNome.requestFocus();

		atualizarBotoes();
	}

	@FXML
	private void handleAbrirCadastroCidade(ActionEvent event) throws IOException {
		FXMLLoader fXMLLoader = new FXMLLoader();
		fXMLLoader.setLocation(getClass().getResource("/view/CadastroCidade.fxml"));
		Stage stage = new Stage();
		Scene scene = new Scene(fXMLLoader.load());
		stage.setScene(scene);
		stage.setResizable(false);
		stage.initStyle(StageStyle.UNDECORATED);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle("Cidades");
		stage.show();
	}

	private void atualizarBotoes() {
		btIncluir.setDisable(getCliente().getId() != null);
		btAlterar.setDisable(getCliente().getId() == null);
		btExcluir.setDisable(getCliente().getId() == null);
	}
	
	public void carregarComboBox() {
		CidadeRepository repository = new CidadeRepository(JPAFactory.getEntityManager());
		List<Cidade> lista = repository.getListNomesCidades();
		
		
		cbCidadeNatal.setItems(FXCollections.observableList(lista));
	}
	
	public Cliente getCliente() {
		if (cliente == null)
			cliente = new Cliente();
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	public Cidade getCidade() {
		if (cidade == null)
			cidade = new Cidade();
		return cidade;
	}

	public void setCidade(Cidade cidade) {
		this.cidade = cidade;
	}
}
