package jm.pivot.app.modelo.pojo;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;

/**
 *
 * @author Luiz Gustavo Stábile de Souza
 */
public class Lancamento {

  NumberFormat nf = NumberFormat.getCurrencyInstance();

  private long id;
  private Date data;
  private String descricao;
  private BigDecimal valor;
  private String observacao;  

  // saldoTotal refere-se ao valor total dos lançamentos até este lançamento, mais o 
  // valor do lançamento (que pode ser positivo ou negativo).
  // Este valor não é persistido no banco de dados, mas é calculado a cada vez 
  // que a relação de lançamentos é solicitada.
  private BigDecimal saldoTotal;
  // saldoAConciliar refere-se ao valor total a conciliar da conta até este 
  // lançamento mais o valor do lançamento, caso o mesmo ainda não tenha sido 
  // conciliado. Assim como acontece com saldoTotal, este valor não 
  // é persistido, mas é calculado a cada vez que a relação de lançamentos é
  // solicitada.
  private BigDecimal saldoAConciliar;

  private boolean conciliado;

  private CategoriaLancamento tipoLancamento;

  public Lancamento() {}

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Date getData() {
    return data;
  }

  public void setData(Date data) {
    this.data = data;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public String getObservacao() {
    return observacao;
  }

  public void setObservacao(String observacao) {
    this.observacao = observacao;
  }
  
  public BigDecimal getValor() {
    return valor;
  }

  public void setValor(BigDecimal valor) {

    // garante que, caso exista um tipo, e este se refira a uma despesa, 
    // o valor será negativo
    if ((tipoLancamento != null)
        && (tipoLancamento.getTipo() == CategoriaLancamento.Tipo.DESPESA))
    {

      if (valor.compareTo(new BigDecimal("0")) > 0) {
        valor = valor.multiply(new BigDecimal("-1"));
      }
    }
    // garante que, caso exista um tipo, e este se refira a uma receita, 
    // o valor será positivo
    else if ((tipoLancamento != null)
        && (tipoLancamento.getTipo() == CategoriaLancamento.Tipo.RECEITA))
    {

      if (valor.compareTo(new BigDecimal("0")) < 0) {
        valor = valor.multiply(new BigDecimal("-1"));
      }
    }

    // simplesmente atribui o valor.
    // se algum dos testes acima foi aplicado, o valor já chega correto
    // neste ponto, caso contrário (se nenhum tipo de lançamento foi atribuído ainda)
    // o valor será ajustado quando o tipo de lançamento for então atribuído.
    this.valor = valor;
  }

  public BigDecimal getSaldoTotal() {
    return saldoTotal;
  }

  public void setSaldoTotal(BigDecimal saldoTotal) {
    this.saldoTotal = saldoTotal;
  }

  public BigDecimal getSaldoAConciliar() {
    return saldoAConciliar;
  }

  public void setSaldoAConciliar(BigDecimal saldoAConciliar) {
    this.saldoAConciliar = saldoAConciliar;
  }

  public boolean isConciliado() {
    return conciliado;
  }

  public void setConciliado(boolean conciliado) {
    this.conciliado = conciliado;
  }

  public CategoriaLancamento getTipoLancamento() {
    return tipoLancamento;
  }

  public void setTipoLancamento(CategoriaLancamento tipoLancamento) {
    this.tipoLancamento = tipoLancamento;

    // garante que uma despesa terá valor negativo
    if (tipoLancamento.getTipo() == CategoriaLancamento.Tipo.DESPESA) {

      if (valor.compareTo(new BigDecimal("0")) > 0) {
        valor = valor.multiply(new BigDecimal("-1"));
      }
    }
    // garante que uma receita terá valor positivo
    else if (tipoLancamento.getTipo() == CategoriaLancamento.Tipo.RECEITA) {

      if (valor.compareTo(new BigDecimal("0")) < 0) {
        valor = valor.multiply(new BigDecimal("-1"));
      }
    }
  }

  public String toString() {
    return descricao;
  }

  
  public boolean equals(Object obj){
    
    boolean equal = false;    
    if(obj instanceof Lancamento){
      Lancamento lan = (Lancamento) obj;
      if(descricao.equals(lan.getDescricao()) && id == lan.getId()){
        equal = true;
      }      
    }    
    return equal;
  } 
  

  public BigDecimal getValorAbsoluto() {
    return valor.abs();
  }
}
