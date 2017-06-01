package jm.pivot.app.modelo.pojo;

/**
 *
 * @author Luiz Gustavo Stábile de Souza
 */
public class CategoriaLancamento {

  /**
   * Enum referente ao Tipo do Lançamento (Despesa ou Receita)
   *
   */
  public static enum Tipo {

    DESPESA("despesa"), RECEITA("receita");

    private final String nome;

    Tipo(String nome) {
      this.nome = nome;
    }
    
    public String getNome(){
      return nome;
    }

    public String toString() {
      return nome;
    }
  };

  private long id;
  private String descricao;
  private Tipo tipo;

  public CategoriaLancamento() {}

  public long getId() {
    return id;
  }

  
  public boolean equals(Object obj){    
    boolean equal = false;    
    if(obj instanceof CategoriaLancamento){
      CategoriaLancamento cat = (CategoriaLancamento) obj;
      if(descricao.equals(cat.getDescricao()) && id == cat.getId()){
        equal = true;
      }      
    }    
    return equal;
  }

  
  
  public void setId(long id) {
    this.id = id;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String nome) {
    this.descricao = nome;
  }

  public Tipo getTipo() {
    return tipo;
  }

  public void setTipo(Tipo tipo) {
    this.tipo = tipo;
  }

  public String toString() {
    return descricao;
  }

  public String validar() {

    String msg = null;

    boolean valido = true;
    StringBuffer alertas = new StringBuffer(
        "Os seguintes problemas foram encontrados: \n\n");

    if ((descricao.equals("")) | (descricao.length() == 0)) {

      alertas.append(" - Nome do tipo de lançamento ausente \n");
      valido = false;
    }

    if (tipo == null) {

      alertas.append(" - Tipo do lançamento (despesa/receita) ausente \n");
      valido = false;
    }

    if (!valido) {
      return alertas.toString();
    }
    else {
      return null;
    }
  }
}
