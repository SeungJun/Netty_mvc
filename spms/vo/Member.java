package spms.vo;

import java.util.Date;

public class Member {
	protected int 		no;
	protected String 	name;
	protected String 	email;
	protected String 	password;
	protected Date		createdDate;
	protected Date		modifiedDate;
	protected int currentPage; 
	protected int pageSize; 
	
	public int getNo() {
		return no;
	}
	public Member setNo(int no) {
		this.no = no;
		return this;
	}
	public String getName() {
		return name;
	}
	public Member setName(String name) {
		this.name = name;
		return this;
	}
	public String getEmail() {
		return email;
	}
	public Member setEmail(String email) {
		this.email = email;
		return this;
	}
	public String getPassword() {
		return password;
	}
	public Member setPassword(String password) {
		this.password = password;
		return this;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public Member setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
		return this;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public Member setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
		return this;
	}
	//Paging 기능 구현 추가 
	public int getCurrentPage(){
		return currentPage; 
	}
	
	public Member setCurrentPage(int currentPage){
		this.currentPage = currentPage; 
		return this; 
	}
	
	public int getPageSize(){
		return pageSize; 
	}
	
	public Member setPageSize(int pageSize){
		this.pageSize = pageSize;
		return this; 
	}
  
}
