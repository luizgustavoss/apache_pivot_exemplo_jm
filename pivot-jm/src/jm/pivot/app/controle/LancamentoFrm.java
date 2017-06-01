package jm.pivot.app.controle;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import jm.pivot.app.modelo.dao.CategoriaLancamentoDAO;
import jm.pivot.app.modelo.dao.LancamentoDAO;
import jm.pivot.app.modelo.pojo.CategoriaLancamento;
import jm.pivot.app.modelo.pojo.Lancamento;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Button.State;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.CalendarButton;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.media.Image;

public class LancamentoFrm extends Frame implements Bindable {

  private Resources resources = null;

  private static LancamentoDAO daoLancamento;
  private CategoriaLancamentoDAO daoCategoria;
  private Lancamento lancamento;
  private NumberFormat numberFormat = new DecimalFormat("#,##0.00");
  private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
  private List<Lancamento> listLancamentos = null;

  @BXML
  private TabPane tabPane;

  // Aba de detalhes
  @BXML
  private TextInput descricao;
  @BXML
  private TextInput valor;
  @BXML
  private Checkbox conciliado;
  @BXML
  private ImageView conciliadoImg;
  @BXML
  private CalendarButton data;
  @BXML
  private RadioButton despesaButton;
  @BXML
  private RadioButton receitaButton;
  @BXML
  private ListButton categoria;
  @BXML
  private TextArea observacoes;
  @BXML
  private BoxPane valorBox;

  // Aba de pesquisa
  @BXML
  private TextInput descricaoPesq;
  @BXML
  private CalendarButton dataDe;
  @BXML
  private CalendarButton dataAte;
  @BXML
  private RadioButton despesaButtonPesq;
  @BXML
  private RadioButton receitaButtonPesq;
  @BXML
  private RadioButton todosButtonPesq;
  @BXML
  private ListButton categoriaPesq;

  @BXML
  private TableView lancamentosPesquisaView;

  public LancamentoFrm() {

    daoLancamento = LancamentoDAO.getInstance();
    daoCategoria = CategoriaLancamentoDAO.getInstance();

    getWindowStateListeners().add(new WindowStateListener.Adapter() {
      @Override
      public void windowOpened(Window window) {
        super.windowOpened(window);
        atualizarListCategoriasDetalhe();
        atualizarListCategoriasPesq();
      }
    });

    Action.getNamedActions().put("salvarAction", new Action() {
      @Override
      public void perform(Component source) {
        if (lancamento == null) {
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

    Action.getNamedActions().put("pesquisarAction", new Action() {
      @Override
      public void perform(Component source) {
        atualizarGrid();
      }
    });
  }

  private MenuHandler menuHandler = new MenuHandler.Adapter() {
    @Override
    public boolean configureContextMenu(Component component, Menu menu, int x,
        int y)
    {
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

            LancamentoWrapper lw = (LancamentoWrapper) tv.getSelectedRow();
            lancamento = lw.getLancamento();
            configurarComponentesTela(lancamento);
            tabPane.setSelectedIndex(0);
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
            LancamentoWrapper lw = (LancamentoWrapper) tv.getSelectedRow();
            lancamento = lw.getLancamento();
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

  private void configurarComponentesTela(Lancamento lanc) {
    descricao.setText(lanc.getDescricao());
    valor.setText(numberFormat.format(lanc.getValorAbsoluto()));
    conciliado.setSelected(lanc.isConciliado());
    conciliadoImg.setVisible(conciliado.isSelected());
    GregorianCalendar c = new GregorianCalendar();
    c.setTime(lanc.getData());
    data.setSelectedDate(new CalendarDate(c));
    despesaButton
        .setSelected(lanc.getTipoLancamento().getTipo() == CategoriaLancamento.Tipo.DESPESA);
    receitaButton.setSelected(!despesaButton.isSelected());
    atualizarListCategoriasDetalhe();
    categoria.setSelectedItem(lanc.getTipoLancamento());
    observacoes.setText(lanc.getObservacao());

  }

  private void atualizarGrid() {

    String desc = descricaoPesq.getText();
    desc = desc.trim();
    Date dataInicial = dataDe.getSelectedDate().toCalendar().getTime();
    Date dataFinal = dataAte.getSelectedDate().toCalendar().getTime();
    CategoriaLancamento.Tipo tipo = null;
    CategoriaLancamento cat = null;
    if (!todosButtonPesq.isSelected()) {
      if (despesaButtonPesq.isSelected()) {
        tipo = CategoriaLancamento.Tipo.DESPESA;
      }
      else if (receitaButtonPesq.isSelected()) {
        tipo = CategoriaLancamento.Tipo.RECEITA;
      }

      cat = (CategoriaLancamento) categoriaPesq.getSelectedItem();
      if (cat != null) {
        if (cat.getDescricao().equals(resources.get("lblTodos").toString())) {
          cat = null;
        }
      }
    }

    try {

      PesquisaTask pesquisaAssincrona = new PesquisaTask(desc, dataInicial,
          dataFinal, tipo, cat);
      TaskListener<List<Lancamento>> taskListener = new TaskListener<List<Lancamento>>()
      {
        @Override
        public void taskExecuted(Task<List<Lancamento>> task) {

          org.apache.pivot.collections.List<LancamentoWrapper> list = new org.apache.pivot.collections.ArrayList<LancamentoWrapper>();

          listLancamentos = task.getResult();

          System.out.println("Pesquisa assíncrona completa!");

          for (Lancamento cl : listLancamentos) {
            LancamentoWrapper wapper = new LancamentoWrapper(cl);
            list.add(wapper);
          }
          lancamentosPesquisaView.setTableData(list);

          if (lancamentosPesquisaView.getTableData().isEmpty()) {
            Prompt.prompt(MessageType.WARNING,
                resources.get("msgNenhumRegistro").toString(),
                LancamentoFrm.this);
          }
        }

        @Override
        public void executeFailed(Task<List<Lancamento>> task) {
          System.err.println(task.getFault());
        }
      };

      pesquisaAssincrona
          .execute(new TaskAdapter<List<Lancamento>>(taskListener));

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

    despesaButton.getButtonStateListeners().add(new ButtonStateListener() {
      @Override
      public void stateChanged(Button arg0, State arg1) {
        atualizarListCategoriasDetalhe();
      }
    });

    receitaButton.getButtonStateListeners().add(new ButtonStateListener() {
      @Override
      public void stateChanged(Button arg0, State arg1) {
        atualizarListCategoriasDetalhe();
      }
    });

    despesaButtonPesq.getButtonStateListeners().add(new ButtonStateListener() {
      @Override
      public void stateChanged(Button arg0, State arg1) {
        atualizarListCategoriasPesq();
      }
    });

    receitaButtonPesq.getButtonStateListeners().add(new ButtonStateListener() {
      @Override
      public void stateChanged(Button arg0, State arg1) {
        atualizarListCategoriasPesq();
      }
    });

    todosButtonPesq.getButtonStateListeners().add(new ButtonStateListener() {
      @Override
      public void stateChanged(Button arg0, State arg1) {
        atualizarListCategoriasPesq();
      }
    });

    try {
      lancamentosPesquisaView.setMenuHandler(menuHandler);
    }
    catch (Exception e) {
      /* tratar */
    }
  }

  private void cancelar() {

    Form.Flag flag = null;

    Form.setFlag(descricao, flag);
    Form.setFlag(valorBox, flag);
    Form.setFlag(categoria, flag);

    lancamento = null;
    descricao.setText("");
    valor.setText("");
    conciliado.setSelected(false);
    conciliadoImg.setVisible(false);
    GregorianCalendar c = new GregorianCalendar();
    c.setTime(new java.util.Date());
    data.setSelectedDate(new CalendarDate(c));
    despesaButton.setSelected(true);
    atualizarListCategoriasDetalhe();
    observacoes.setText("");
  }

  private void atualizarListCategoriasDetalhe() {

    try {
      categoria.getListData().clear();
      List<CategoriaLancamento> listCategorias = null;
      if (receitaButton.isSelected()) {
        listCategorias = daoCategoria
            .obterTiposLancamento(CategoriaLancamento.Tipo.RECEITA);
      }
      else {
        listCategorias = daoCategoria
            .obterTiposLancamento(CategoriaLancamento.Tipo.DESPESA);
      }
      org.apache.pivot.collections.List<CategoriaLancamento> list = new org.apache.pivot.collections.ArrayList<CategoriaLancamento>();

      for (CategoriaLancamento cl : listCategorias) {
        list.add(cl);
      }
      categoria.setListData(list);
      categoria.setSelectedIndex(0);
    }
    catch (Exception e) {
      /* tratar */
    }
  }

  private void atualizarListCategoriasPesq() {

    try {
      categoriaPesq.getListData().clear();

      CategoriaLancamento todasCategorias = new CategoriaLancamento();
      todasCategorias.setDescricao(resources.get("lblTodos").toString());

      org.apache.pivot.collections.List<CategoriaLancamento> list = new org.apache.pivot.collections.ArrayList<CategoriaLancamento>();

      list.add(todasCategorias);
      if (!todosButtonPesq.isSelected()) {
        List<CategoriaLancamento> listCategorias = null;
        if (receitaButtonPesq.isSelected()) {
          listCategorias = daoCategoria
              .obterTiposLancamento(CategoriaLancamento.Tipo.RECEITA);
        }
        else {
          listCategorias = daoCategoria
              .obterTiposLancamento(CategoriaLancamento.Tipo.DESPESA);
        }
        for (CategoriaLancamento cl : listCategorias) {
          list.add(cl);
        }
      }
      categoriaPesq.setListData(list);
      categoriaPesq.setSelectedIndex(0);
    }
    catch (Exception e) {
      /* tratar */
    }
  }

  private void salvarNovo() {

    try {

      Form.Flag flagDesc = null;
      Form.Flag flagVal = null;
      Form.Flag flagCat = null;

      String desc = descricao.getText().trim();
      String val = valor.getText().trim();

      if (desc == null || "".equals(desc.trim())) {
        flagDesc = new Form.Flag(MessageType.ERROR, resources.get(
            "campoRequerido").toString());
      }
      Form.setFlag(descricao, flagDesc);

      if (val == null || "".equals(val.trim())) {
        flagVal = new Form.Flag(MessageType.ERROR, resources.get(
            "campoRequerido").toString());
      }
      Form.setFlag(valorBox, flagVal);

      if (categoria == null) {
        flagCat = new Form.Flag(MessageType.ERROR, resources.get(
            "campoRequerido").toString());
      }
      Form.setFlag(categoria, flagCat);

      if (flagDesc != null || flagVal != null || flagCat != null) { return; }

      lancamento = new Lancamento();
      lancamento.setDescricao(desc);
      lancamento.setConciliado(conciliado.isSelected());
      lancamento.setData(data.getSelectedDate().toCalendar().getTime());
      lancamento.setObservacao(observacoes.getText());
      lancamento.setValor(new BigDecimal(numberFormat.parse(val).toString()));
      lancamento.setTipoLancamento((CategoriaLancamento) categoria
          .getSelectedItem());

      daoLancamento.cadastrar(lancamento);
      cancelar();
      Prompt.prompt(MessageType.INFO, resources.get("sucessoCadastro")
          .toString(), this);
    }
    catch (Exception e) {
      e.printStackTrace();
      Prompt.prompt(MessageType.ERROR, resources.get("falhaCadastro")
          .toString(), this);
    }

  }

  private void salvarEdicao() {

    try {

      Form.Flag flagDesc = null;
      Form.Flag flagVal = null;
      Form.Flag flagCat = null;

      String desc = descricao.getText();
      String val = valor.getText();

      if (desc == null || "".equals(desc.trim())) {
        flagDesc = new Form.Flag(MessageType.ERROR, resources.get(
            "campoRequerido").toString());
      }
      Form.setFlag(descricao, flagDesc);

      if (val == null || "".equals(val.trim())) {
        flagVal = new Form.Flag(MessageType.ERROR, resources.get(
            "campoRequerido").toString());
      }
      Form.setFlag(valorBox, flagVal);

      if (categoria == null) {
        flagCat = new Form.Flag(MessageType.ERROR, resources.get(
            "campoRequerido").toString());
      }
      Form.setFlag(categoria, flagCat);

      if (flagDesc != null || flagVal != null || flagCat != null) { return; }

      lancamento.setDescricao(desc);
      lancamento.setConciliado(conciliado.isSelected());
      lancamento.setData(data.getSelectedDate().toCalendar().getTime());
      lancamento.setObservacao(observacoes.getText());
      lancamento.setValor(new BigDecimal(numberFormat.parse(val).toString()));
      lancamento.setTipoLancamento((CategoriaLancamento) categoria
          .getSelectedItem());

      daoLancamento.alterar(lancamento);
      cancelar();
      Prompt.prompt(MessageType.INFO,
          resources.get("sucessoEdicao").toString(), this);
    }
    catch (Exception e) {
      e.printStackTrace();
      Prompt.prompt(MessageType.ERROR, resources.get("falhaEdicao").toString(),
          this);
    }
  }

  private void excluir() {

    try {
      daoLancamento.excluir(lancamento);
      cancelar();
      Prompt.prompt(MessageType.INFO, resources.get("sucessoExclusao")
          .toString(), this);
    }
    catch (Exception e) {
      Prompt.prompt(MessageType.ERROR, resources.get("falhaExclusao")
          .toString(), this);
    }
  }

  public class LancamentoWrapper {

    private Lancamento lancamento;
    private Image tipoImg;
    private Image conciliadoImg;

    public LancamentoWrapper(Lancamento lanc) {
      this.lancamento = lanc;

      try {
        if (CategoriaLancamento.Tipo.DESPESA == lancamento.getTipoLancamento()
            .getTipo())
        {
          tipoImg = Image.load(getClass().getResource("despesa.png"));
        }
        else {
          tipoImg = Image.load(getClass().getResource("receita.png"));
        }

        if (lancamento.isConciliado()) {
          conciliadoImg = Image.load(getClass().getResource("conciliado.png"));
        }
      }
      catch (TaskExecutionException e) {
        /* tratar */
      }
    }

    public String getData() {
      return dateFormat.format(lancamento.getData());
    }

    public Lancamento getLancamento() {
      return lancamento;
    }

    public long getId() {
      return lancamento.getId();
    }

    public String getDescricao() {
      return lancamento.getDescricao();
    }

    public Image getTipoImg() {
      return tipoImg;
    }

    public Image getConciliadoImg() {
      return conciliadoImg;
    }

    public String getValor() {
      return numberFormat.format(lancamento.getValor().doubleValue());
    }

    public String getSaldo() {
      return numberFormat.format(lancamento.getSaldoTotal().doubleValue());
    }

    public String getSaldoConciliar() {
      return numberFormat.format(lancamento.getSaldoAConciliar().doubleValue());
    }
  }

  public static class PesquisaTask extends Task<List<Lancamento>> {

    private String desc;
    private Date dataInicial;
    private Date dataFinal;
    private CategoriaLancamento.Tipo tipo;
    private CategoriaLancamento cat;

    public PesquisaTask(String desc, Date dataInicial, Date dataFinal,
        CategoriaLancamento.Tipo tipo, CategoriaLancamento cat)
    {
      this.desc = desc;
      this.dataInicial = dataInicial;
      this.dataFinal = dataFinal;
      this.tipo = tipo;
      this.cat = cat;
    }

    @Override
    public List<Lancamento> execute() throws TaskExecutionException {

      List<Lancamento> listLancamentos = null;

      try {
        listLancamentos = daoLancamento.obterLancamentos(tipo, desc, cat,
            dataInicial, dataFinal);
      }
      catch (Exception exception) {
        throw new TaskExecutionException(exception);
      }

      return listLancamentos;
    }
  }
}