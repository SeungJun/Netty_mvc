package spms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;

public class DBConnectionPool {
  String url;
  String username;
  String password;
  ArrayList<Connection> connList = new ArrayList<Connection>();
  
  public DBConnectionPool(String driver, String url, 
      String username, String password) throws Exception {
    this.url = url;
    this.username = username;
    this.password = password;
    
    //driver 연결 막기 
    if(!"driver".equals(driver)){
    	Class.forName(driver);
    }
  }
  
  public Connection getConnection() throws Exception {
    if (connList.size() > 0) {
      Connection conn = connList.remove(0); 
      if (conn.isValid(10)) {
        return conn;
      }
    }
    return DriverManager.getConnection(url, username, password);
  }
  
//커넥션 객체를 쓰고 풀에 반환하는 메서드 
  public void returnConnection(Connection conn) throws Exception {
    connList.add(conn);
  }
  
  public void closeAll() {
    for(Connection conn : connList) {
      try{conn.close();} catch (Exception e) {}
    }
  }
}
