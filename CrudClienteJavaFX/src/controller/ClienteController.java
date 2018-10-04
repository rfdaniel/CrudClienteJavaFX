package controller;

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
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import model.Cliente;
import repository.ClienteRepository;

public class ClienteController implements Initializable {

	private Cliente cliente;

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
	private TableColumn<Cliente, String> tcEmailCliente;
	
    @FXML
    private TableColumn<Cliente, LocalDate> dataAniversario;

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
		
		//VERIFICANDO SE Ã‰ O BOTÃƒO PRINCIPAL QUE FOI CLIADO
		if (event.getButton().equals(MouseButton.PRIMARY)) {
			//VERIFICANDO SE A QUANTIDADE DE CLIQUES NO BOTÃƒO PRIMÃ�RIO Ã‰ IGUAL A 2
			if (event.getClickCount() == 2) {

				cliente = tvClientes.getSelectionModel().getSelectedItem();

				tfCpf.setText(cliente.getCpf());
				tfNome.setText(cliente.getNome());
				tfEndereco.setText(cliente.getEndereco());
				tfEmail.setText(cliente.getEmail());
				dpAniversario.setValue(cliente.getDataAniversario());

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
		
		cliente.setCpf(tfCpf.getText());
		cliente.setNome(tfNome.getText());
		cliente.setEndereco(tfEndereco.getText());
		cliente.setEmail(tfEmail.getText());
		cliente.setDataAniversario(dpAniversario.getValue());

		ClienteRepository repository = new ClienteRepository(JPAFactory.getEntityManager());
		
		repository.getEntityManager().getTransaction().begin();
		repository.save(cliente);
		repository.getEntityManager().getTransaction().commit();
		repository.getEntityManager().close();
		
		handleLimpar(event);
	}

	@FXML
	void handleExcluir(ActionEvent event) {
		
		ClienteRepository repository = new ClienteRepository(JPAFactory.getEntityManager());
		
		//MENSAGEM DE ALERTA PARA O USUÃ�RIO CONFIRMAR UMA EXCLUSÃƒO
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmação");
		alert.setHeaderText("Está operação excluirá todas as informações selecionadas da base de dados.");
		alert.setContentText("Deseja realmente excluir?");
		//Capturar as resposta do usuÃ¡rio sobre a mensagem de confirmaÃ§Ã£o
		Optional<ButtonType> resposta = alert.showAndWait();
		if(resposta.get().equals(ButtonType.OK)) {
		
			// Iniciando a transaÃ§Ã£o
			repository.getEntityManager().getTransaction().begin();
			repository.remove(cliente);
			repository.getEntityManager().getTransaction().commit();
			repository.getEntityManager().close();
			
			handleLimpar(event);
		}else if(resposta.get().equals(ButtonType.CANCEL)) {
				
		}
	}

	@FXML
	void handleIncluir(ActionEvent event) {
		cliente = new Cliente(tfCpf.getText(), tfNome.getText(), tfEndereco.getText(), tfEmail.getText(), dpAniversario.getValue());

		ClienteRepository repository = new ClienteRepository(JPAFactory.getEntityManager());
		
		// Iniciando a transação
		repository.getEntityManager().getTransaction().begin();
		repository.save(cliente);
		repository.getEntityManager().getTransaction().commit();
		repository.getEntityManager().close();
		
		handleLimpar(event);
	}

	@FXML
	void handleLimpar(ActionEvent event) {
		tfCpf.setText("");
		tfNome.setText("");
		tfEmail.setText("");
		tfEndereco.setText("");
		dpAniversario.setValue(null);
		
		//LIMPANDO AS INFORMAÃ‡Ã•ES DO CLIENTE
		cliente = new Cliente();
		
		tfNome.requestFocus();
		
		atualizarBotoes();
	}
	
	private void atualizarBotoes() {
		btIncluir.setDisable(cliente.getId() != null);
		btAlterar.setDisable(cliente.getId() == null);
		btExcluir.setDisable(cliente.getId() == null);
	}
}
