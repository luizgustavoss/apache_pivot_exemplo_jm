package jm.pivot.app.controle;

import java.io.IOException;
import java.util.Locale;

import jm.pivot.app.modelo.dao.FactoryConexao;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;

public class Main implements Application {

  private Display display = null;
  private Window window = null;
  private Locale locale = null;
  public static final String LANGUAGE_KEY = "language";
  private CategoriaFrm categoria = null; 
  private LancamentoFrm lancamento = null;
   
  
  @Override
  public void startup(Display display, Map<String, String> properties)
      throws Exception
  {
    this.display = display;

    BXMLSerializer bxmlSerializer = new BXMLSerializer();
    
    String language = properties.get(LANGUAGE_KEY);
    locale = (language == null) ? Locale.getDefault() : new Locale(language);
    Resources resources = new Resources(getClass().getName(), locale);

    try {
      window = (Window) bxmlSerializer.readObject(
          Main.class.getResource("main.bxml"), resources);
      window.open(display);
    }
    catch (SerializationException exception) {
      /* tratar */ 
    }
    catch (IOException exception) {
      /* tratar */ 
    }
  }

  public Main() {

    Action.getNamedActions().put("abrirLancamentos", new Action() {
      @Override
      public void perform(Component source) {
        try {
          
          Resources resources = new Resources(LancamentoFrm.class.getName(), locale);
          BXMLSerializer bxmlSerializer = new BXMLSerializer();
          if(lancamento == null){
            lancamento = (LancamentoFrm) bxmlSerializer.readObject(
                LancamentoFrm.class.getResource("lancamento.bxml"), resources);
          }         
          // só abrirá a frame se ela estiver fechada (abre apenas uma)
          if(lancamento.isClosed()){
            lancamento.open(window);
          }
        }
        catch (IOException e) {
          /* tratar */ 
        }
        catch (SerializationException e) {
          /* tratar */ 
        }
      }
    });

    Action.getNamedActions().put("abrirCategorias", new Action() {
      @Override
      public void perform(Component source) {
        try {
          BXMLSerializer bxmlSerializer = new BXMLSerializer();
          Resources resources = new Resources(CategoriaFrm.class.getName(), locale);
          // só irá criar a tela uma única vez
          if(categoria == null){
            categoria = (CategoriaFrm) bxmlSerializer.readObject(
                CategoriaFrm.class.getResource("categoria.bxml"), resources);           
          }         
          // só abrirá a frame se ela estiver fechada (abre apenas uma)
          if(categoria.isClosed()){
            categoria.open(window);
          }
        }
        catch (IOException e) {
          /* tratar */ 
        }
        catch (SerializationException e) {
       /* tratar */ 
        }
      }
    });

    Action.getNamedActions().put("sair", new Action() {
      @Override
      public void perform(Component source) {
        try {
          FactoryConexao.shutdown();
          DesktopApplicationContext.exit();
        }
        catch (Exception e) {
          /* tratar */ 
        }        
      }
    });
  }

  @Override
  public boolean shutdown(boolean optional) {
    for (int i = display.getLength() - 1; i >= 0; i--) {
      Window window = (Window) display.get(i);
      window.close();
    }

    return false;
  }

  @Override
  public void suspend() {}

  @Override
  public void resume() {}

  public static void main(String[] args) {
    DesktopApplicationContext.main(Main.class, args);
  }
  
}
