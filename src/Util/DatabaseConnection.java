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

    // URL do SQLite: Cria um arquivo chamado 'game_data.db' na pasta do projeto
    private static final String DB_URL = "jdbc:sqlite:game_data.db"; 

    public static Connection getConnection() {
        try {
            // Registra o driver do SQLite
            Class.forName("org.sqlite.JDBC");

            // Tenta estabelecer a conexão (cria o arquivo se não existir)
            Connection connection = DriverManager.getConnection(DB_URL);
            
            // Garante que as tabelas existam no banco local
            createTablesIfNotExists(connection);
            
            return connection;

        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados local: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite não encontrado! Verifique se o .jar está no projeto.");
            e.printStackTrace();
            return null;
        }
    }

    // Método para criar as tabelas automaticamente no arquivo local
    private static void createTablesIfNotExists(Connection conn) {
        String sqlJogador = "CREATE TABLE IF NOT EXISTS tbl_jogador ("
                + "id_jogador INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nome TEXT NOT NULL UNIQUE"
                + ");";

        String sqlPontuacao = "CREATE TABLE IF NOT EXISTS tbl_pontuacao ("
                + "id_pontuacao INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "id_jogador INTEGER,"
                + "pontos INTEGER,"
                + "tempo_corrido REAL,"
                + "FOREIGN KEY(id_jogador) REFERENCES tbl_jogador(id_jogador)"
                + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlJogador);
            stmt.execute(sqlPontuacao);
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabelas: " + e.getMessage());
        }
    }

    // Metodo simples para testar a conexão 
    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Conexão com o banco local criada e fechada com sucesso.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static int findOrCreatePlayer(String playerName) {

        String selectSql = "SELECT id_jogador FROM tbl_jogador WHERE nome = ?";
        String insertSql = "INSERT INTO tbl_jogador (nome) VALUES (?)";

        try (Connection conn = getConnection()) {
            if (conn == null) return -1;

            // --- 1. Tenta Encontrar o Jogador ---
            try (PreparedStatement pstmtSelect = conn.prepareStatement(selectSql)) {
                pstmtSelect.setString(1, playerName);

                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    if (rs.next()) {
                        int playerID = rs.getInt("id_jogador");
                        System.out.println("Jogador '" + playerName + "' encontrado com ID: " + playerID);
                        return playerID; 
                    }
                }
            }

            // --- 2. Jogador NÃO Encontrado, crie um novo ---
            System.out.println("Jogador '" + playerName + "' não encontrado. Criando novo...");

            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmtInsert.setString(1, playerName);
                int affectedRows = pstmtInsert.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet genKeys = pstmtInsert.getGeneratedKeys()) {
                        if (genKeys.next()) {
                            int playerID = genKeys.getInt(1);
                            System.out.println("Jogador '" + playerName + "' criado com ID: " + playerID);
                            return playerID; 
                        }
                    }
                }
            }

            return -1;

        } catch (SQLException e) {
            System.err.println("Erro ao buscar ou criar jogador:");
            e.printStackTrace();
            return -1; 
        }
    }

    public static void saveScore(int playerID, int pontos, double tempoCorrido) {
        String sql = "INSERT INTO tbl_pontuacao (id_jogador, pontos, tempo_corrido) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) return;

            pstmt.setInt(1, playerID);
            pstmt.setInt(2, pontos);        
            pstmt.setDouble(3, tempoCorrido); 

            pstmt.executeUpdate();
            System.out.printf("Score salvo! ID: %d, Distância: %d m, Tempo: %.2f s%n", playerID, pontos, tempoCorrido);

        } catch (SQLException e) {
            System.err.println("Erro ao salvar pontuação:");
            e.printStackTrace();
        }
    }

    public static List<String> getTopScores() {
        List<String> topScores = new ArrayList<>();

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
                int distancia = rs.getInt("pontos"); 

                String scoreLine = String.format("%d. %s - %d m (%.2f s)", rank, nome, distancia, tempo);
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
