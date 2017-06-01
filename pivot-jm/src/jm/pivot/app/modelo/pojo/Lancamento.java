package jm.pivot.app.modelo.pojo;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;

/**
 *
 * @author Luiz Gustavo St�bile de Souza
 */
public class Lancamento {

  NumberFormat nf = NumberFormat.getCurrencyInstance();

  private long id;
  private Date data;
  private String descricao;
  private BigDecimal valor;
  private String observacao;  

  // saldoTotal refere-se ao valor total dos lan�amentos at� este lan�amento, mais o 
  // valor do lan�amento (que pode ser positivo ou negativo).
  // Este valor n�o � persistido no banco de dados, mas � calculado a cada vez 
  // que a rela��o de lan�amentos � solicitada.
  private BigDecimal saldoTotal;
  // saldoAConciliar refere-se ao valor total a conciliar da conta at� este 
  // lan�amento mais o valor do lan�amento, caso o mesmo ainda n�o tenha sido 
  // conciliado. Assim como acontece com saldoTotal, este valor n�o 
  // � persistido, mas � calculado a cada vez que a rela��o de lan�amentos �
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
    // o valor ser� negativo
    if ((tipoLancamento != null)
        && (tipoLancamento.getTipo() == CategoriaLancamento.Tipo.DESPESA))
    {

      if (valor.compareTo(new BigDecimal("0")) > 0) {
        valor = valor.multiply(new BigDecimal("-1"));
      }
    }
    // garante que, caso exista um tipo, e este se refira a uma receita, 
    // o valor ser� positivo
    else if ((tipoLancamento != null)
        && (tipoLancamento.getTipo() == CategoriaLancamento.Tipo.RECEITA))
    {

      if (valor.compareTo(new BigDecimal("0")) < 0) {
        valor = valor.multiply(new BigDecimal("-1"));
      }
    }

    // simplesmente atribui o valor.
    // se algum dos testes acima foi aplicado, o valor j� chega correto
    // neste ponto, caso contr�rio (se nenhum tipo de lan�amento foi atribu�do ainda)
    // o valor ser� ajustado quando o tipo de lan�amento for ent�o atribu�do.
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

    // garante que uma despesa ter� valor negativo
    if (tipoLancamento.getTipo() == CategoriaLancamento.Tipo.DESPESA) {

      if (valor.compareTo(new BigDecimal("0")) > 0) {
        valor = valor.multiply(new BigDecimal("-1"));
      }
    }
    // garante que uma receita ter� valor positivo
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
