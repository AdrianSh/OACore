package es.jovenesadventistas.Arnion.Process.Binders.Transfers;

public class StringTransfer implements Transfer {
	private String data;
	
	public StringTransfer(String data) {
		this.setData(data);
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
