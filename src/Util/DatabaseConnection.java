package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/game_data"; // DEFINA O IP E A PORTA DO SEU BANCO DE DADOS MYSQL
    private static final String DB_USER = "XXXXX"; //SUBSTITUA O XXXXX PELO SEU USUARIO DO BANCO
    private static final String DB_PASS = "XXXXX"; //SUBISTITUA O XXXXX PELA SENHA SENHA DO BANCO

    public static Connection getConnection() {
        try {
            // Isso registra o driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Tenta estabelecer a conexão
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Conexão com o banco de dados estabelecida!");
            return connection;

        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL não encontrado! Verifique se o .jar está no projeto.");
            e.printStackTrace();
            return null;
        }
    }

    // Metodo simples para testar a conexão 
    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Conexão fechada com sucesso.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static int findOrCreatePlayer(String playerName) {

        String selectSql = "SELECT id_jogador FROM tbl_jogador WHERE nome = ?";
        String insertSql = "INSERT INTO tbl_jogador (nome) VALUES (?)";

        //try-with-resources para a conexão (fecha automaticamente)
        try (Connection conn = getConnection()) {

            if (conn == null) return -1;

            // --- 1. Tenta Encontrar o Jogador ---
            // (try-with-resources para o PreparedStatement e ResultSet)
            try (PreparedStatement pstmtSelect = conn.prepareStatement(selectSql)) {
                pstmtSelect.setString(1, playerName);

                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    if (rs.next()) {
                        // --- Jogador ENCONTRADO ---
                        int playerID = rs.getInt("id_jogador");
                        System.out.println("Jogador '" + playerName + "' encontrado com ID: " + playerID);
                        return playerID; // Retorna o ID existente
                    }
                }
            }

            // --- 2. Jogador NÃO Encontrado, crie um novo ---
            System.out.println("Jogador '" + playerName + "' não encontrado. Criando novo...");

            // (try-with-resources para o PreparedStatement de inserção)
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmtInsert.setString(1, playerName);
                int affectedRows = pstmtInsert.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet genKeys = pstmtInsert.getGeneratedKeys()) {
                        if (genKeys.next()) {
                            int playerID = genKeys.getInt(1);
                            System.out.println("Jogador '" + playerName + "' criado com ID: " + playerID);
                            return playerID; // Retorna o NOVO ID
                        }
                    }
                }
            }

            // Se algo falhou na inserção
            return -1;

        } catch (SQLException e) {
            System.err.println("Erro ao buscar ou criar jogador:");
            e.printStackTrace();
            return -1; // Retorna -1 em caso de erro
        }
    }

    public static void saveScore(int playerID, int pontos, double tempoCorrido) {
        // Metodo para salvar 'pontos' (distância) e 'tempo_corrido'
        String sql = "INSERT INTO tbl_pontuacao (id_jogador, pontos, tempo_corrido) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) return;

            pstmt.setInt(1, playerID);
            pstmt.setInt(2, pontos);        // Salva a distância na coluna pontos
            pstmt.setDouble(3, tempoCorrido); // Salva o tempo

            pstmt.executeUpdate();
            System.out.printf("Score salvo! ID: %d, Distância: %d m, Tempo: %.2f s%n", playerID, pontos, tempoCorrido);

        } catch (SQLException e) {
            System.err.println("Erro ao salvar pontuação:");
            e.printStackTrace();
        }
    }

    /**
     * Busca o Top 10 ordenado por PONTOS (Distância) DESC (Maior para menor).
     */
    public static List<String> getTopScores() {
        List<String> topScores = new ArrayList<>();

        //ORDER BY p.pontos DESC (Quem foi mais longe aparece primeiro)
        String sql = "SELECT j.nome, p.tempo_corrido, p.pontos " +
                "FROM tbl_pontuacao p " +
                "JOIN tbl_jogador j ON p.id_jogador = j.id_jogador " +
                "ORDER BY p.pontos DESC " +
                "LIMIT 10";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (conn == null) return topScores;

            int rank = 1;
            while (rs.next()) {
                String nome = rs.getString("nome");
                double tempo = rs.getDouble("tempo_corrido");
                int distancia = rs.getInt("pontos"); // A coluna pontos agora guarda a distância

                // Formata: "1. Nome - 1500m (30.5s)"
                String scoreLine = String.format("%d. %s - %d m (%.2f s)",
                        rank, nome, distancia, tempo);
                topScores.add(scoreLine);
                rank++;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar top scores:");
            e.printStackTrace();
        }
        return topScores;
    }
}

