package jm.pivot.app.modelo.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/** Classe Factory Singleton responsável pela conexão com o banco de dados.
 *
 * @author Luiz Gustavo Stábile de Souza
 *
 */
public class FactoryConexao {
    
    private static FactoryConexao factoryConexao;
    
    private static String URL = "jdbc:hsqldb:file:c:/data/pivotapp/pivotapp";
    private static final String DRIVER = "org.hsqldb.jdbcDriver";
    private static final String USUARIO = "sa";
    private static final String SENHA = "";
    
    
    static{
      
      try{
        Class.forName(DRIVER);
      }
      catch(Exception e){
          // TODO
      }
    }
  
    
    
    /** Método responsável por retornar um objeto de conexão 
     *  com o banco de dados.
     *
     * @return um objeto <b>Connection</b> que representa uma conexão com o
     * banco de dados ou null, se nenhuma conexão for conseguida.
     *
     * @throws Exception
     */
    public static Connection getConnection() throws Exception{
        
        Connection conn = null;
        
        try{                        
            conn = DriverManager.getConnection(URL, USUARIO, SENHA);            
        } catch(Exception e){
            throw e;
        }         
        return conn;
    }
    
    /** Método responsável por encerrar a sessão 
     * com o servidor do banco de dados. O servidor grava os dados
     * em arquivo e efetua um shutdown adequado.
     * Deve ser chamado ao término do programa.
     *
     * @throws Exception
     */
    public static void shutdown() throws Exception {

        Connection conn = null;
        
        try{            
            conn = FactoryConexao.getConnection();
            Statement st = conn.createStatement();
            st.execute("SHUTDOWN");
            conn.close();                
        } catch(Exception e){
            throw e;
        }
    }    
    
}
