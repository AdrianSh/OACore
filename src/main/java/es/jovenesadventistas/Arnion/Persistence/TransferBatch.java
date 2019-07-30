package es.jovenesadventistas.Arnion.Persistence;

import java.util.Stack;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class TransferBatch {
	@Id 
    @GeneratedValue 
    private Long id;
	
	private Stack<String> transfers;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Stack<String> getTransfers() {
		return transfers;
	}
	
	public boolean addTransfer(String tJson) {
		return this.transfers.add(tJson);
	}

	public void setTransfers(Stack<String> transfers) {
		this.transfers = transfers;
	}

	
}
