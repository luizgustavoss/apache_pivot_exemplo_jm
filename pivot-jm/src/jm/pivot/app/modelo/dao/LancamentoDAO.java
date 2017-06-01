package jm.pivot.app.modelo.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jm.pivot.app.modelo.pojo.CategoriaLancamento;
import jm.pivot.app.modelo.pojo.Lancamento;

public class LancamentoDAO {

  private static LancamentoDAO daoLancamento;
  private Connection conn;

  private LancamentoDAO() {}

  public static LancamentoDAO getInstance() {

    if (daoLancamento == null) {
      daoLancamento = new LancamentoDAO();
    }
    return daoLancamento;
  }

  public synchronized void cadastrar(Lancamento lancamento)
      throws SQLException, Exception
  {

    try {
      conn = FactoryConexao.getConnection();
      conn.setAutoCommit(false);

      String SQL = "INSERT INTO lancamento "
          + "(data, descricao, valor, conciliado, tipo, observacao) "
          + "VALUES (?, ?, ?, ?, ?, ?)";

      PreparedStatement stmt = conn.prepareStatement(SQL);
      stmt.setDate(1, new java.sql.Date(lancamento.getData().getTime()));
      stmt.setString(2, lancamento.getDescricao());
      stmt.setBigDecimal(3, lancamento.getValor());
      stmt.setBoolean(4, lancamento.isConciliado());
      stmt.setLong(5, lancamento.getTipoLancamento().getId());
      stmt.setString(6, lancamento.getObservacao());
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

  public synchronized void excluir(Lancamento lancamento) throws SQLException,
      Exception
  {

    try {
      conn = FactoryConexao.getConnection();
      conn.setAutoCommit(false);

      String SQL = "DELETE FROM lancamento WHERE id = ?";
      PreparedStatement stmt = conn.prepareStatement(SQL);
      stmt.setLong(1, lancamento.getId());
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

  public synchronized void alterar(Lancamento lancamento) throws SQLException,
      Exception
  {

    try {

      conn = FactoryConexao.getConnection();
      conn.setAutoCommit(false);

      String SQL = "UPDATE lancamento SET descricao = ?, conciliado = ?, "
          + " tipo = ?, valor = ?, observacao = ?  WHERE id = ?";

      PreparedStatement stmt = conn.prepareStatement(SQL);

      stmt.setString(1, lancamento.getDescricao());
      stmt.setBoolean(2, lancamento.isConciliado());
      stmt.setLong(3, lancamento.getTipoLancamento().getId());
      stmt.setBigDecimal(4, lancamento.getValor());
      stmt.setString(5, lancamento.getObservacao());
      stmt.setLong(6, lancamento.getId());

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

  public synchronized List<Lancamento> obterLancamentos(
      CategoriaLancamento.Tipo tipo, String descricao,
      CategoriaLancamento categoria, Date dataInicial, Date dataFinal)
      throws SQLException, Exception
  {

    Lancamento umLancamento = null;
    List<Lancamento> lancamentos = new ArrayList<Lancamento>();
    CategoriaLancamentoDAO daoTipoLancamento = CategoriaLancamentoDAO
        .getInstance();
    Map<Long, CategoriaLancamento> tiposLancamento = daoTipoLancamento
        .obterMapaTiposLancamento();

    BigDecimal saldoAConciliarConta = new BigDecimal("0");
    BigDecimal saldoTotalConta = new BigDecimal("0");

    ResultSet resultSet = null;
    PreparedStatement stmt = null;
    StringBuilder SQL = new StringBuilder("");

    try {

      conn = FactoryConexao.getConnection();

      // selecionar a soma dos valores absolutos dos lançamentos a conciliar
      SQL = new StringBuilder("SELECT SUM(ABS(valor)) AS a_conciliar ");
      SQL.append(" FROM (SELECT valor ");
      SQL.append(" FROM lancamento ");
      SQL.append(" WHERE data < ? AND conciliado = false ) ");

      stmt = conn.prepareStatement(SQL.toString(),
          ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

      stmt.setDate(1, new java.sql.Date(dataInicial.getTime()));

      resultSet = stmt.executeQuery();

      resultSet.first();
      saldoAConciliarConta = resultSet.getBigDecimal("a_conciliar");
      if (saldoAConciliarConta == null) {
        saldoAConciliarConta = new BigDecimal("0");
      }

      SQL = new StringBuilder("SELECT SUM(valor) AS saldo_total ");
      SQL.append(" FROM (SELECT valor ");
      SQL.append("FROM lancamento ");
      SQL.append(" WHERE data < ? AND conciliado = true )");

      stmt = conn.prepareStatement(SQL.toString(),
          ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

      stmt.setDate(1, new java.sql.Date(dataInicial.getTime()));

      resultSet = stmt.executeQuery();

      resultSet.first();
      saldoTotalConta = resultSet.getBigDecimal("saldo_total");
      if (saldoTotalConta == null) {
        saldoTotalConta = new BigDecimal("0");
      }

      SQL = new StringBuilder("SELECT * FROM lancamento l");
      if (tipo != null) {
        SQL.append(", categoria_lancamento c ");
      }
      SQL.append(" WHERE ");
      SQL.append(" (l.data BETWEEN ? AND ?) ");

      if (descricao != null && !"".equals(descricao.trim())) {
        SQL.append(" and l.descricao like ? ");
      }

      if (tipo != null) {

        SQL.append(" and c.tipo = ? and  c.id = l.tipo ");

        if (categoria != null) {
          SQL.append(" and c.id = ? ");
        }
      }
      SQL.append(" ORDER BY data, id ");

      System.out.println(SQL.toString());

      stmt = conn.prepareStatement(SQL.toString(),
          ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

      int index = 1;

      stmt.setDate(index, new java.sql.Date(dataInicial.getTime()));

      index++;
      stmt.setDate(index, new java.sql.Date(dataFinal.getTime()));

      if (descricao != null && !"".equals(descricao.trim())) {
        index++;
        stmt.setString(index, descricao);
      }

      if (tipo != null) {
        index++;
        stmt.setString(index, tipo.getNome());

        if (categoria != null) {
          index++;
          stmt.setLong(index, categoria.getId());
        }
      }

      resultSet = stmt.executeQuery();

      while (resultSet.next()) {

        umLancamento = new Lancamento();

        umLancamento.setId(resultSet.getInt("id"));
        umLancamento.setData(resultSet.getDate("data"));
        umLancamento.setDescricao(resultSet.getString("descricao"));
        umLancamento.setValor(resultSet.getBigDecimal("valor"));
        umLancamento.setConciliado(resultSet.getBoolean("conciliado"));
        umLancamento.setObservacao(resultSet.getString("observacao"));

        long t = resultSet.getLong("tipo");
        umLancamento.setTipoLancamento(tiposLancamento.get(t));

        if (!umLancamento.isConciliado()) {
          // o saldo a conciliar é um valor absoluto 
          // (acumulativo independentemente de ser positivo ou negativo)
          saldoAConciliarConta = saldoAConciliarConta.add(umLancamento
              .getValor().abs());
        }
        else {
          saldoTotalConta = saldoTotalConta.add(umLancamento.getValor());
        }

        umLancamento.setSaldoAConciliar(saldoAConciliarConta);
        umLancamento.setSaldoTotal(saldoTotalConta);

        lancamentos.add(umLancamento);
        umLancamento = null;
      }
      stmt.close();
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      conn.close();
    }
    return lancamentos;
  }
}
