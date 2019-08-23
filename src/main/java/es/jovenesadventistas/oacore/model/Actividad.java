package es.jovenesadventistas.oacore.model;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class Actividad {
	@Id
	private ObjectId id;
	
	private String estado;
	private User user;
	private Date createdAt;
	private Date updatedAt;

	
	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId id) {
		if (id != null)
			this.id = id;
	}
	
	public static Actividad createActividad(String estado, User user, Date createdAt) {
		Actividad a = new Actividad();
		a.estado = estado;
		a.user = user;
		a.createdAt = a.updatedAt = createdAt;
		return a;
	}
	
	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}
	
	
	public User getUser(){
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public Date getUpdatedAt(){
		return updatedAt;
	}
	
	public void setUpdatedAt(Date date){
		this.updatedAt = date;
	}
	
	public Date getCreatedAt(){
		return createdAt;
	}
	
	public void setCreatedAt(Date date){
		this.updatedAt = this.createdAt = date;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Actividad) {
			Actividad o = (Actividad) obj;
			return o.id == this.id;
		}
		return false;
	}
}
