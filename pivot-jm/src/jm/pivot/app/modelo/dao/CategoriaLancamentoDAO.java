package jm.pivot.app.modelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jm.pivot.app.modelo.pojo.CategoriaLancamento;

public class CategoriaLancamentoDAO {

  private static CategoriaLancamentoDAO daoTipoLancamento;
  private Connection conn;

  private CategoriaLancamentoDAO() {}

  public static CategoriaLancamentoDAO getInstance() {

    if (daoTipoLancamento == null) {
      daoTipoLancamento = new CategoriaLancamentoDAO();
    }

    return daoTipoLancamento;
  }

  public synchronized void cadastrar(CategoriaLancamento tipoLancamento)
      throws SQLException, Exception
  {

    try {

      conn = FactoryConexao.getConnection();
      conn.setAutoCommit(false);

      String SQL = "INSERT INTO categoria_lancamento (descricao, tipo) VALUES (?, ?)";

      PreparedStatement stmt = conn.prepareStatement(SQL);
      stmt.setString(1, tipoLancamento.getDescricao());
      stmt.setString(2, tipoLancamento.getTipo().toString());
      stmt.executeUpdate();
      stmt.close();
      conn.commit();
    }
    catch (Exception e) {
      conn.rollback();
      throw e;
    }
    finally {
      conn.close();
    }
  }

  public synchronized void excluir(CategoriaLancamento tipoLancamento)
      throws SQLException, Exception
  {

    try {

      conn = FactoryConexao.getConnection();
      conn.setAutoCommit(false);

      String SQL = "DELETE FROM categoria_lancamento WHERE id = ?";
      PreparedStatement stmt = conn.prepareStatement(SQL);
      stmt.setLong(1, tipoLancamento.getId());
      stmt.executeUpdate();
      stmt.close();
      conn.commit();
    }
    catch (Exception e) {
      conn.rollback();
      throw e;
    }
    finally {
      conn.close();
    }
  }

  public synchronized void alterar(CategoriaLancamento tipoLancamento)
      throws SQLException, Exception
  {

    try {

      conn = FactoryConexao.getConnection();
      conn.setAutoCommit(false);

      String SQL = "UPDATE categoria_lancamento SET descricao = ?, tipo = ? WHERE id = ?";

      PreparedStatement stmt = conn.prepareStatement(SQL);
      stmt.setString(1, tipoLancamento.getDescricao());
      stmt.setString(2, tipoLancamento.getTipo().toString());
      stmt.setLong(3, tipoLancamento.getId());
      stmt.executeUpdate();
      stmt.close();
      conn.commit();
    }
    catch (Exception e) {
      conn.rollback();
      throw e;
    }
    finally {
      conn.close();
    }
  }

  public synchronized List<CategoriaLancamento> obterTiposLancamento()
      throws SQLException, Exception
  {

    CategoriaLancamento umTipoLancamento = null;
    List<CategoriaLancamento> tiposLancamento = new ArrayList<CategoriaLancamento>();

    try {

      conn = FactoryConexao.getConnection();

      String SQL = "SELECT * FROM categoria_lancamento ORDER BY descricao";
      PreparedStatement stmt = conn.prepareStatement(SQL,
          ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

      ResultSet resultSet = stmt.executeQuery();

      while (resultSet.next()) {

        umTipoLancamento = new CategoriaLancamento();

        umTipoLancamento.setId(resultSet.getLong("id"));
        umTipoLancamento.setDescricao(resultSet.getString("descricao"));

        String tipo = resultSet.getString("tipo");

        for (CategoriaLancamento.Tipo t : CategoriaLancamento.Tipo.values()) {
          if (t.toString().equals(tipo)) {
            umTipoLancamento.setTipo(t);
            break;
          }
        }

        tiposLancamento.add(umTipoLancamento);
        umTipoLancamento = null;
      }

      stmt.close();

    }
    catch (Exception e) {
      throw e;
    }
    finally {
      conn.close();
    }

    return tiposLancamento;
  }

  public synchronized List<CategoriaLancamento> obterTiposLancamento(
      CategoriaLancamento.Tipo type) throws SQLException, Exception
  {

    CategoriaLancamento umTipoLancamento = null;
    List<CategoriaLancamento> tiposLancamento = new ArrayList<CategoriaLancamento>();

    try {

      conn = FactoryConexao.getConnection();

      String SQL = "SELECT * FROM categoria_lancamento where tipo = ? ORDER BY descricao";
      PreparedStatement stmt = conn.prepareStatement(SQL,
          ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

      stmt.setString(1, type.getNome());

      ResultSet resultSet = stmt.executeQuery();

      while (resultSet.next()) {

        umTipoLancamento = new CategoriaLancamento();

        umTipoLancamento.setId(resultSet.getLong("id"));
        umTipoLancamento.setDescricao(resultSet.getString("descricao"));

        String tipo = resultSet.getString("tipo");

        for (CategoriaLancamento.Tipo t : CategoriaLancamento.Tipo.values()) {
          if (t.toString().equals(tipo)) {
            umTipoLancamento.setTipo(t);
            break;
          }
        }
        tiposLancamento.add(umTipoLancamento);
        umTipoLancamento = null;
      }
      stmt.close();
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      conn.close();
    }
    return tiposLancamento;
  }

  public synchronized Map<Long, CategoriaLancamento> obterMapaTiposLancamento()
      throws SQLException, Exception
  {

    CategoriaLancamento umTipoLancamento = null;
    Map<Long, CategoriaLancamento> tiposLancamento = new HashMap<Long, CategoriaLancamento>();

    try {

      conn = FactoryConexao.getConnection();

      String SQL = "SELECT * FROM categoria_lancamento ORDER BY descricao";
      PreparedStatement stmt = conn.prepareStatement(SQL,
          ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

      ResultSet resultSet = stmt.executeQuery();

      while (resultSet.next()) {

        umTipoLancamento = new CategoriaLancamento();

        umTipoLancamento.setId(resultSet.getInt("id"));
        umTipoLancamento.setDescricao(resultSet.getString("descricao"));

        String tipo = resultSet.getString("tipo");

        for (CategoriaLancamento.Tipo t : CategoriaLancamento.Tipo.values()) {
          if (t.toString().equals(tipo)) {
            umTipoLancamento.setTipo(t);
            break;
          }
        }

        tiposLancamento.put(umTipoLancamento.getId(), umTipoLancamento);
        umTipoLancamento = null;
      }
      stmt.close();
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      conn.close();
    }
    return tiposLancamento;
  }
}
