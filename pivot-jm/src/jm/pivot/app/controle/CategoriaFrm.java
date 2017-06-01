package jm.pivot.app.controle;

import java.net.URL;
import java.util.List;

import jm.pivot.app.modelo.dao.CategoriaLancamentoDAO;
import jm.pivot.app.modelo.pojo.CategoriaLancamento;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.media.Image;

public class CategoriaFrm extends Frame implements Bindable {

  private Resources resources = null;

  private CategoriaLancamentoDAO dao;
  private CategoriaLancamento categoria;

  @BXML private TextInput descricao;
  @BXML private RadioButton despesaButton;
  @BXML private RadioButton receitaButton;
  @BXML private TableView categoriasView;

  public CategoriaFrm() {
    dao = CategoriaLancamentoDAO.getInstance();

    Action.getNamedActions().put("salvarAction", new Action() {
      @Override
      public void perform(Component source) {
        if (categoria == null) {
          salvarNovo();
        }
        else {
          salvarEdicao();
        }
        atualizarGrid();
      }
    });

    Action.getNamedActions().put("cancelarAction", new Action() {
      @Override
      public void perform(Component source) {
        cancelar();
      }
    });

  }

  private void cancelar() {

    Form.Flag flag = null;
    categoria = null;
    Form.setFlag(descricao, flag);
    descricao.setText("");
    despesaButton.setSelected(true);
  }

  private void salvarNovo() {

    Form.Flag flagDesc = null;
    String desc = descricao.getText();

    if (desc == null || "".equals(desc.trim())) {
      flagDesc = new Form.Flag(MessageType.ERROR, resources.get(
          "campoRequerido").toString());
    }
    Form.setFlag(descricao, flagDesc);

    if (flagDesc != null) { return; }

    categoria = new CategoriaLancamento();
    categoria.setDescricao(desc);
    CategoriaLancamento.Tipo tipo = null;
    tipo = receitaButton.isSelected() ? CategoriaLancamento.Tipo.RECEITA
        : CategoriaLancamento.Tipo.DESPESA;
    categoria.setTipo(tipo);

    try {
      dao.cadastrar(categoria);
      cancelar();
      Prompt.prompt(MessageType.INFO, resources.get("sucessoCadastro")
          .toString(), this);
    }
    catch (Exception e) {
      Prompt.prompt(MessageType.ERROR, resources.get("falhaCadastro")
          .toString(), this);
    }

  }

  private void salvarEdicao() {

    Form.Flag flagDesc = null;
    String desc = descricao.getText();

    if (desc == null || "".equals(desc.trim())) {
      flagDesc = new Form.Flag(MessageType.ERROR, resources.get(
          "campoRequerido").toString());
    }
    Form.setFlag(descricao, flagDesc);

    if (flagDesc != null) { return; }

    categoria.setDescricao(desc);
    CategoriaLancamento.Tipo tipo = null;
    tipo = receitaButton.isSelected() ? CategoriaLancamento.Tipo.RECEITA
        : CategoriaLancamento.Tipo.DESPESA;
    categoria.setTipo(tipo);

    try {
      dao.alterar(categoria);
      cancelar();
      Prompt.prompt(MessageType.INFO,
          resources.get("sucessoEdicao").toString(), this);
    }
    catch (Exception e) {
      Prompt.prompt(MessageType.ERROR, resources.get("falhaEdicao").toString(),
          this);
    }
  }

  private void excluir() {

    try {
      dao.excluir(categoria);
      cancelar();
      Prompt.prompt(MessageType.INFO, resources.get("sucessoExclusao")
          .toString(), this);
    }
    catch (Exception e) {
      Prompt.prompt(MessageType.ERROR, resources.get("falhaExclusao")
          .toString(), this);
    }
  }

  private MenuHandler menuHandler = new MenuHandler.Adapter() {
    @Override
    public boolean configureContextMenu(Component component, Menu menu, int x,
        int y)
    {
      //      final Component descendant = getDescendantAt(x, y);
      final TableView tv = (TableView) component;

      Menu.Section menuSection = new Menu.Section();
      menu.getSections().add(menuSection);

      try {

        Menu.Item editarMenuItem = new Menu.Item();
        editarMenuItem
            .setButtonData(new ButtonData(Image.load(getClass().getResource(
                "editar.gif")), resources.get("menEditar").toString()));
        editarMenuItem.setAction(new Action() {
          @Override
          public void perform(Component source) {
            CategoriaLancamentoWrapper clw = (CategoriaLancamentoWrapper) tv
                .getSelectedRow();
            categoria = (CategoriaLancamento) clw.getCategoria();
            configurarComponentesTela(categoria);
          }
        });
        menuSection.add(editarMenuItem);

        Menu.Item excluirMenuItem = new Menu.Item();
        excluirMenuItem.setButtonData(new ButtonData(Image.load(getClass()
            .getResource("excluir.gif")), resources.get("menExcluir")
            .toString()));
        excluirMenuItem.setAction(new Action() {
          @Override
          public void perform(Component source) {
            CategoriaLancamentoWrapper clw = (CategoriaLancamentoWrapper) tv
                .getSelectedRow();
            categoria = (CategoriaLancamento) clw.getCategoria();
            excluir();
            atualizarGrid();
          }
        });
        menuSection.add(excluirMenuItem);
      }
      catch (TaskExecutionException e) {
        /* tratar */
      }
      return false;
    }
  };

  private void configurarComponentesTela(CategoriaLancamento cl) {
    descricao.setText(cl.getDescricao());
    despesaButton.setSelected(cl.getTipo() == CategoriaLancamento.Tipo.DESPESA);
    receitaButton.setSelected(!despesaButton.isSelected());

  }

  private void atualizarGrid() {

    org.apache.pivot.collections.List<CategoriaLancamentoWrapper> list = new org.apache.pivot.collections.ArrayList<CategoriaLancamentoWrapper>();

    List<CategoriaLancamento> listCategorias;
    try {
      listCategorias = dao.obterTiposLancamento();
      for (CategoriaLancamento cl : listCategorias) {

        String desc = resources.get(cl.getTipo().getNome()).toString();
        CategoriaLancamentoWrapper wapper = new CategoriaLancamentoWrapper(cl,
            desc);
        list.add(wapper);
      }
      categoriasView.setTableData(list);
    }
    catch (Exception e) {
      /* tratar */
    }

  }

  @Override
  public void initialize(Map<String, Object> namespace, URL location,
      Resources resources)
  {
    this.resources = resources;

    try {
      categoriasView.setMenuHandler(menuHandler);
      atualizarGrid();
    }
    catch (Exception e) {
      /* tratar */
    }
  }

  public class CategoriaLancamentoWrapper {

    private CategoriaLancamento categoria;
    private Image tipoImg;
    private String desc;

    public CategoriaLancamentoWrapper(CategoriaLancamento cat, String desc) {
      this.categoria = cat;
      this.desc = desc;

      try {
        if (CategoriaLancamento.Tipo.DESPESA == categoria.getTipo()) {
          tipoImg = Image.load(getClass().getResource("despesa.png"));
        }
        else {
          tipoImg = Image.load(getClass().getResource("receita.png"));
        }
      }
      catch (TaskExecutionException e) {
        /* tratar */
      }
    }

    public CategoriaLancamento getCategoria() {
      return categoria;
    }

    public long getId() {
      return categoria.getId();
    }

    public String getDescricao() {
      return categoria.getDescricao();
    }

    public String getTipoDesc() {
      return desc;
    }

    public Image getTipoImg() {
      return tipoImg;
    }

  }
}
