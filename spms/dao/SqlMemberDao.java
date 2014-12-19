package spms.dao;

// 애노테이션 적용 
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import spms.annotation.Component;
import spms.util.DBConnectionPool;
import spms.vo.Member;
import io.netty.http.snoop.*;

@Component("memberDao")
public class SqlMemberDao implements MemberDao {

	DBConnectionPool connPool;

	public SqlMemberDao(DBConnectionPool connPool) {
		this.connPool = connPool;
	}

/*	public void setDBConnectionPool(DBConnectionPool connPool){
		this.connPool = connPool;
	}*/

	public List<Member> selectList() throws Exception {
		Connection conn = null;
		//		PreparedStatement pstmt = null;
		CallableStatement cs = null ; 
		ResultSet rs = null;
		
/*		List<Member> list = new ArrayList<Member>(); 
		Member m; 
		for(int i=0; i<10; i++)
		{
			m = new Member().setNo(i).setName("name_" +i);
			list.add(m);
		}*/
		

		try {
			//conn = ds.getConnection();
			conn = connPool.getConnection(); 
			//저장 프로시저로 작업
			cs = conn.prepareCall("execute dbo.SP_mvctest @func=?");

			cs.setString(1, "selectList");

			//			stmt = conn.createStatement();
			//			rs = stmt.executeQuery(
			//					"SELECT MNO,MNAME,EMAIL,CRE_DATE" + 
			//							" FROM MEMBERS" +
			//					" ORDER BY MNO ASC");

			rs = cs.executeQuery();
			ArrayList<Member> members = new ArrayList<Member>();

			while(rs.next()) {
				members.add(new Member()

				.setNo(rs.getInt("MNO"))
				.setName(rs.getString("MNAME"))
				.setEmail(rs.getString("EMAIL"))
				.setCreatedDate(rs.getDate("CRE_DATE"))	);
			}

			return members;

		} catch (Exception e) {
			throw e;

		} finally {
			try {if (rs != null) rs.close();} catch(Exception e) {}
			//      try {if (stmt != null) stmt.close();} catch(Exception e) {}
			try {if (conn != null) conn.close();} catch(Exception e) {}
			try {if (cs != null) cs.close();} catch(Exception e) {}
		}
	}

	public int insert(Member member) throws Exception  {
		Connection conn = null;
		//    PreparedStatement pstmt = null;
		CallableStatement cs = null; 

		try {
//			conn = ds.getConnection();
			conn = connPool.getConnection(); 
			cs = conn.prepareCall("execute dbo.SP_mvctest @func=?,@email=?,@pwd=?, @mname=?");
//			stmt = conn.prepareStatement(
//					"INSERT INTO MEMBERS(EMAIL,PWD,MNAME,CRE_DATE,MOD_DATE)"
//							+ " VALUES (?,?,?,getutcdate(),getutcdate())");
//			stmt.setString(1, member.getEmail());
//			stmt.setString(2, member.getPassword());
//			stmt.setString(3, member.getName());
//			return stmt.executeUpdate();

			cs.setString(1, "insert");
			cs.setString(2, member.getEmail());
			cs.setString(3, member.getPassword());
			cs.setString(4, member.getName());

			return cs.executeUpdate(); 

		} catch (Exception e) {
			throw e;

		} finally {
//      try {if (stmt != null) stmt.close();} catch(Exception e) {}
			try {if (cs != null)cs.close();}catch(Exception e){}
			try {if (conn != null) conn.close();} catch(Exception e) {}
		}
	}

	public int delete(int no) throws Exception {  
		Connection conn = null;
//    Statement stmt = null;
		CallableStatement cs = null ;

		try {
//			conn = ds.getConnection();
			conn = connPool.getConnection(); 
			cs = conn.prepareCall("execute dbo.SP_mvctest @func=?, @mno=?");
			cs.setString(1, "delete");
			cs.setInt(2, no);
			/*stmt = conn.createStatement();
      return stmt.executeUpdate(
          "DELETE FROM MEMBERS WHERE MNO=" + no);*/
			return cs.executeUpdate(); 

		} catch (Exception e) {
			throw e;

		} finally {
			//      try {if (stmt != null) stmt.close();} catch(Exception e) {}
			try {if (cs != null) cs.close();} catch(Exception e) {}
			try {if (conn != null) conn.close();} catch(Exception e) {}
		}
	}

	public Member selectOne(int no) throws Exception { 
		Connection conn = null;
//    Statement stmt = null;
		CallableStatement cs = null; 
		ResultSet rs = null;
		try {
//			conn = ds.getConnection();
			conn = connPool.getConnection(); 
			cs = conn.prepareCall("execute dbo.SP_mvctest @func=?, @mno=?");
			cs.setString(1, "selectOne");
			cs.setInt(2, no); 
			/*stmt = connection.createStatement();
      rs = stmt.executeQuery(
          "SELECT MNO,EMAIL,MNAME,CRE_DATE FROM MEMBERS" + 
              " WHERE MNO=" + no);*/    
			rs = cs.executeQuery(); 
			if (rs.next()) {
				return new Member()
				.setNo(rs.getInt("MNO"))
				.setEmail(rs.getString("EMAIL"))
				.setName(rs.getString("MNAME"))
				.setCreatedDate(rs.getDate("CRE_DATE"));

			} else {
//				throw new Exception("해당 번호의 회원을 찾을 수 없습니다.");
				return null; 
			}

		} catch (Exception e) {
			throw e;
		} finally {
			try {if (rs != null) rs.close();} catch(Exception e) {}
			//      try {if (stmt != null) stmt.close();} catch(Exception e) {}
			try {if (cs!= null) cs.close();} catch(Exception e) {}
			try {if (conn != null) conn.close();} catch(Exception e) {}
		}
	}

	public int update(Member member) throws Exception { 
		Connection conn = null;
//    PreparedStatement stmt = null;
		CallableStatement cs = null; 
		try {
//			conn = ds.getConnection();
			conn = connPool.getConnection(); 
			cs = conn.prepareCall("execute dbo.SP_mvctest @func=?, @mno=?, @mname=?, @email=?, @mod_date=?");
			cs.setString(1, "update");
			cs.setInt(2, member.getNo());
			cs.setString(3, member.getEmail());
			cs.setString(4,  member.getName());
			cs.setDate(5, (Date)member.getModifiedDate());
			/*stmt = conn.prepareStatement(
          "UPDATE MEMBERS SET EMAIL=?,MNAME=?,MOD_DATE=getutcdate()"
              + " WHERE MNO=?");
      stmt.setString(1, member.getEmail());
      stmt.setString(2, member.getName());
      stmt.setInt(3, member.getNo());
      return stmt.executeUpdate();*/
			return cs.executeUpdate(); 

		} catch (Exception e) {
			throw e;

		} finally {
			//      try {if (stmt != null) stmt.close();} catch(Exception e) {}
			try {if (conn!= null) conn.close();} catch(Exception e) {}
			try {if (cs!= null) cs.close();} catch(Exception e) {}
		}
	}

	public Member exist(String email, String password) throws Exception {
		Connection conn = null;
		//    PreparedStatement stmt = null;
		CallableStatement cs = null; 
		ResultSet rs = null;

		try {
			//			conn = ds.getConnection();
			conn = connPool.getConnection(); 
			cs = conn.prepareCall("execute dbo.SP_mvctest @func=?,@email=?, @pwd=?");
			cs.setString(1,"exist" );
			cs.setString(2, email);
			cs.setString(3, password);
			/*stmt = conn.prepareStatement(
          "SELECT MNAME,EMAIL FROM MEMBERS"
              + " WHERE EMAIL=? AND PWD=?");
      stmt.setString(1, email);
      stmt.setString(2, password);
      rs = stmt.executeQuery();*/
			rs = cs.executeQuery(); 
			if (rs.next()) {
				return new Member()
				.setName(rs.getString("MNAME"))
				.setEmail(rs.getString("EMAIL"));
			} else {
				return null;
			}
		} catch (Exception e) {
			throw e;

		} finally {
			try {if (rs != null) rs.close();} catch (Exception e) {}
			//      try {if (stmt != null) stmt.close();} catch (Exception e) {}

			try {if (conn != null) conn.close();} catch(Exception e) {}
			try {if (cs != null) cs.close();} catch(Exception e) {}
		}
	}

	public List<Member> paging(int currentPage, int pageSize)throws Exception{
		Connection conn = null;
		CallableStatement cs = null; 
		ResultSet rs = null;
		ArrayList<Member> members = new ArrayList<Member>();

		try {
//			conn = ds.getConnection();
			conn = connPool.getConnection(); 
			cs = conn.prepareCall("execute dbo.SP_pagingQuery @currentPage=?, @pageSize=?"); 
			cs.setInt(1, currentPage);
			cs.setInt(2, pageSize);
			rs = cs.executeQuery(); 

			while(rs.next()){
				members.add(new Member()

				.setNo(rs.getInt("MNO"))
				.setName(rs.getString("MNAME"))
				.setEmail(rs.getString("EMAIL"))
				.setCreatedDate(rs.getDate("CRE_DATE"))	);
			}
		} catch (Exception e) {
			throw e; 
		}finally{
			try{if(rs != null)rs.close();}catch(Exception e){}
			try{if(conn != null)conn.close();}catch(Exception e){}
			try{if(cs != null)cs.close();}catch(Exception e){}
		}
		return members; 
	}
}
