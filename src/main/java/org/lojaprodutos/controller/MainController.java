package org.lojaprodutos.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.lojaprodutos.model.Categoria;
import org.lojaprodutos.model.Produto;
import org.lojaprodutos.model.ProdutoDAO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MainController {

    // Campos do formulário
    @FXML private TextField inputCodigo;
    @FXML private TextField inputNome;
    @FXML private TextArea inputDescricao;
    @FXML private ComboBox<String> comboCategoria;
    @FXML private DatePicker inputDataFab;
    @FXML private DatePicker inputDataVal;
    @FXML private TextField inputPrecoCompra;
    @FXML private TextField inputPrecoVenda;
    @FXML private Spinner<Integer> spinnerQtd;

    // Tabela e colunas
    @FXML private TableView<Produto> tabelaProdutos;
    @FXML private TableColumn<Produto, String> colCodigo;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, String> colCategoria;
    @FXML private TableColumn<Produto, BigDecimal> colPrecoVenda;
    @FXML private TableColumn<Produto, Integer> colQtd;
    @FXML private TableColumn<Produto, LocalDate> colValidade;

    private ProdutoDAO dao;
    private ObservableList<Produto> listaProdutos;

    @FXML
    public void initialize() {
        try {
            dao = new ProdutoDAO();
        } catch (Exception e) {
            mostrarAlerta("Erro", "Falha ao iniciar DAO: " + e.getMessage());
        }

        comboCategoria.setItems(FXCollections.observableArrayList(
                "Alimentos", "Limpeza", "Higiene", "Bebidas"
        ));

        colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCodigo()));
        colNome.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNome()));
        colCategoria.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategoria().getNome()));
        colPrecoVenda.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPrecoVenda()));
        colQtd.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getQuantidadeEstoque()));
        colValidade.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDataValidade()));

        spinnerQtd.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000, 1));

        carregarTodos();

        tabelaProdutos.getSelectionModel().selectedItemProperty().addListener((obs, oldV, novo) -> {
            if (novo != null) preencherCampos(novo);
        });
    }

    /* ===================== CRUD UI ===================== */

    @FXML
    public void salvarProduto() {
        try {
            Produto novo = criarProdutoDosCampos();
            if (dao.buscarPorCodigo(novo.getCodigo()) != null) {
                mostrarAlerta("Erro", "Código já cadastrado.");
                return;
            }
            dao.adicionar(novo);
            carregarTodos();
            limparFormulario();
        } catch (Exception e) {
            mostrarAlerta("Erro ao salvar", e.getMessage());
        }
    }

    @FXML
    public void editarProduto() {
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta("Selecione", "Escolha um produto para editar.");
            return;
        }
        try {
            Produto atualizado = criarProdutoDosCampos();
            dao.remover(selecionado.getCodigo());
            dao.adicionar(atualizado);
            carregarTodos();
            limparFormulario();
        } catch (Exception e) {
            mostrarAlerta("Erro ao editar", e.getMessage());
        }
    }

    @FXML
    public void removerProduto() {
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta("Selecione", "Escolha um produto para remover.");
            return;
        }
        dao.remover(selecionado.getCodigo());
        carregarTodos();
        limparFormulario();
    }

    @FXML
    public void consultarPorCodigo() {
        String codigo = inputCodigo.getText();
        Produto produto = dao.buscarPorCodigo(codigo);
        if (produto != null) {
            preencherCampos(produto);
        } else {
            mostrarAlerta("Não encontrado", "Produto não encontrado.");
        }
    }

    @FXML
    public void carregarTodos() {
        if (dao == null) return;
        listaProdutos = FXCollections.observableArrayList(dao.listar());
        tabelaProdutos.setItems(listaProdutos);
    }

    @FXML
    public void exportarCSV() {
        dao.exportarCSV();
        mostrarAlerta("Sucesso", "Produtos exportados para CSV.");
    }

    @FXML
    private void onListar() {
        atualizarTabela();
        alertInfo("Listagem", "Lista atualizada.");
    }

    @FXML
    private void onConsultarPorCodigo() {
        String codigo = inputCodigo.getText();
        Produto produto = dao.buscarPorCodigo(codigo); // Busca o produto pelo código
        if (produto != null) {
            preencherCampos(produto); // Preenche o formulário com os dados do produto
            alertInfo("Consulta", "Produto encontrado.");
        } else {
            alertInfo("Consulta", "Produto não encontrado.");
        }
    }

    private void alertInfo(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void atualizarTabela() {
        listaProdutos = FXCollections.observableArrayList(dao.listar());
        tabelaProdutos.setItems(listaProdutos);
    }

    @FXML
    public void limparFormulario() {
        inputCodigo.clear();
        inputNome.clear();
        inputDescricao.clear();
        inputDataFab.setValue(null);
        inputDataVal.setValue(null);
        inputPrecoCompra.clear();
        inputPrecoVenda.clear();
        spinnerQtd.getValueFactory().setValue(1);
        comboCategoria.getSelectionModel().clearSelection();
        tabelaProdutos.getSelectionModel().clearSelection();
    }

    @FXML
    public void abrirRelatorios() {
        // Adicionando um exemplo de relatório
        StringBuilder sb = new StringBuilder();

        List<Produto> vencidos = dao.listarVencidos();
        sb.append("🛑 Produtos vencidos:\n");
        for (Produto p : vencidos) {
            sb.append("- ").append(p.getNome())
                    .append(" (Val: ").append(p.getDataValidade()).append(")\n");
        }

        sb.append("\n📈 Média de Preço por Categoria:\n");
        Map<String, Double> mediaMap = dao.mediaPrecoVendaPorCategoria();
        mediaMap.forEach((cat, media) ->
                sb.append("- ").append(cat).append(": R$ ")
                        .append(String.format("%.2f", media)).append("\n"));

        sb.append("\n📦 Estoque por Categoria:\n");
        Map<String, Integer> estoqueMap = dao.estoqueTotalPorCategoria();
        estoqueMap.forEach((cat, qtd) ->
                sb.append("- ").append(cat).append(": ").append(qtd).append(" unidades\n"));

        mostrarAlerta("Relatórios", sb.toString());
    }


    private Produto criarProdutoDosCampos() {
        String codigo = inputCodigo.getText();
        String nome = inputNome.getText();
        String descricao = inputDescricao.getText();
        LocalDate fabricacao = inputDataFab.getValue();
        LocalDate validade = inputDataVal.getValue();

        if (codigo == null || !codigo.matches("[A-Za-z0-9]{8}")) {
            mostrarAlerta("Erro", "O código deve ter exatamente 8 caracteres alfanuméricos.");
            return null;
        }
        if (nome == null || nome.isBlank() || nome.length() < 3) {
            mostrarAlerta("Erro", "O nome do produto não pode estar vazio e deve ter pelo menos 3 caracteres.");
            return null;
        }
        if (fabricacao == null || fabricacao.isAfter(LocalDate.now())) {
            mostrarAlerta("Erro", "A data de fabricação não pode ser posterior à data atual.");
            return null;
        }
        if (validade == null || validade.isBefore(fabricacao)) {
            mostrarAlerta("Erro", "A data de validade não pode ser anterior à data de fabricação.");
            return null;
        }

        BigDecimal precoCompra = parseBigDecimal(inputPrecoCompra.getText());
        BigDecimal precoVenda = parseBigDecimal(inputPrecoVenda.getText());
        if (precoVenda.compareTo(precoCompra) <= 0) {
            mostrarAlerta("Erro", "O preço de venda deve ser maior que o preço de compra.");
            return null;
        }

        int qtd = spinnerQtd.getValue() == null || spinnerQtd.getValue() < 0 ? 0 : spinnerQtd.getValue();
        if (qtd < 0) {
            mostrarAlerta("Erro", "A quantidade em estoque não pode ser negativa.");
            return null;
        }

        String categoriaNome = comboCategoria.getValue() == null ? "Sem categoria" : comboCategoria.getValue();
        Categoria cat = new Categoria(0, categoriaNome, "", "");

        return new Produto(codigo, nome, descricao, fabricacao, validade, precoCompra, precoVenda, qtd, cat);
    }

    private BigDecimal parseBigDecimal(String txt) {
        if (txt == null || txt.isBlank()) return BigDecimal.ZERO;
        String norm = txt.trim().replace(",", ".");
        return new BigDecimal(norm);
    }

    private void preencherCampos(Produto p) {
        inputCodigo.setText(p.getCodigo());
        inputNome.setText(p.getNome());
        inputDescricao.setText(p.getDescricao());
        inputDataFab.setValue(p.getDataFabricacao());
        inputDataVal.setValue(p.getDataValidade());
        inputPrecoCompra.setText(p.getPrecoCompra() == null ? "" : p.getPrecoCompra().toPlainString());
        inputPrecoVenda.setText(p.getPrecoVenda() == null ? "" : p.getPrecoVenda().toPlainString());
        spinnerQtd.getValueFactory().setValue(p.getQuantidadeEstoque());
        comboCategoria.setValue(p.getCategoria().getNome());
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
