package org.lojaprodutos.model;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ProdutoDAO {

    private List<Produto> produtos;

    public ProdutoDAO() {
        this.produtos = new ArrayList<>();
    }

    public void adicionar(Produto produto) {
        produtos.add(produto);
    }

    public void remover(String codigo) {
        produtos.removeIf(p -> p.getCodigo().equals(codigo));
    }

    public List<Produto> listar() {
        return new ArrayList<>(produtos); // Retorna todos os produtos cadastrados
    }


    public Produto buscarPorCodigo(String codigo) {
        return produtos.stream()
                .filter(p -> p.getCodigo().equals(codigo))
                .findFirst()
                .orElse(null);
    }

    public void exportarCSV() {
        try (FileWriter writer = new FileWriter("produtos.csv")) {
            writer.write("codigo;nome;descricao;dataFabricacao;dataValidade;precoCompra;precoVenda;quantidade;categoria\n");
            for (Produto p : produtos) {
                writer.write(String.format("%s;%s;%s;%s;%s;%.2f;%.2f;%d;%s\n",
                        p.getCodigo(),
                        p.getNome(),
                        p.getDescricao(),
                        p.getDataFabricacao(),
                        p.getDataValidade(),
                        p.getPrecoCompra(),
                        p.getPrecoVenda(),
                        p.getQuantidadeEstoque(),
                        p.getCategoria().getNome()
                ));
            }
        } catch (IOException e) {
            System.err.println("Erro ao exportar CSV: " + e.getMessage());
        }
    }

    // Listar produtos vencidos
    public List<Produto> listarVencidos() {
        return produtos.stream()
                .filter(p -> p.getDataValidade() != null && p.getDataValidade().isBefore(LocalDate.now()))
                .collect(Collectors.toList());
    }

    // Calcular média de preço de venda por categoria
    public Map<String, Double> mediaPrecoVendaPorCategoria() {
        return produtos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria().getNome(),
                        Collectors.averagingDouble(p -> p.getPrecoVenda().doubleValue())
                ));
    }

    // Calcular estoque total por categoria
    public Map<String, Integer> estoqueTotalPorCategoria() {
        return produtos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria().getNome(),
                        Collectors.summingInt(Produto::getQuantidadeEstoque)
                ));
    }

    // Filtrar produtos próximos ao vencimento (dentro dos próximos 'dias' dias)
    public List<Produto> filtrarProximosAoVencimento(int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(dias);
        return produtos.stream()
                .filter(p -> p.getDataValidade() != null)
                .filter(p -> !p.getDataValidade().isBefore(hoje) && !p.getDataValidade().isAfter(limite))
                .sorted(Comparator.comparing(Produto::getDataValidade))
                .collect(Collectors.toList());
    }

    // Filtrar produtos com estoque abaixo de um limite
    public List<Produto> filtrarEstoqueBaixo(int limite) {
        return produtos.stream()
                .filter(p -> p.getQuantidadeEstoque() < limite)
                .sorted(Comparator.comparingInt(Produto::getQuantidadeEstoque))
                .collect(Collectors.toList());
    }

    // Calcular a margem de lucro média por categoria
    public Map<String, Double> margemLucroMediaPorCategoria() {
        return produtos.stream()
                .filter(p -> p.getPrecoVenda() != null && p.getPrecoVenda().doubleValue() > 0.0)
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria().getNome(),
                        Collectors.averagingDouble(p -> {
                            var venda  = p.getPrecoVenda();
                            var compra = p.getPrecoCompra() == null ? BigDecimal.ZERO : p.getPrecoCompra();
                            return venda.subtract(compra)
                                    .divide(venda, 6, BigDecimal.ROUND_HALF_UP)
                                    .doubleValue();
                        })
                ));
    }

    // Listar produtos agrupados por setor de categoria
    public Map<String, List<Produto>> listarPorSetorCategoria() {
        return produtos.stream()
                .collect(Collectors.groupingBy(p -> {
                    String setor = p.getCategoria() != null ? p.getCategoria().getSetor() : null;
                    return (setor == null || setor.isBlank()) ? "Sem setor" : setor;
                }));
    }

    // Carregar dados de um arquivo CSV
    public void carregarCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("produtos.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("codigo")) continue; // Pular cabeçalho
                String[] dados = line.split(";");
                String codigo = dados[0];
                String nome = dados[1];
                String descricao = dados[2];
                LocalDate dataFab = LocalDate.parse(dados[3]);
                LocalDate dataVal = LocalDate.parse(dados[4]);
                BigDecimal precoCompra = new BigDecimal(dados[5]);
                BigDecimal precoVenda = new BigDecimal(dados[6]);
                int qtd = Integer.parseInt(dados[7]);
                Categoria categoria = new Categoria(0, dados[8], "", "");
                Produto p = new Produto(codigo, nome, descricao, dataFab, dataVal, precoCompra, precoVenda, qtd, categoria);
                produtos.add(p);
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar CSV: " + e.getMessage());
        }
    }
}
