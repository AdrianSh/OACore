package es.jovenesadventistas.Arnion.Process.Binders.Transfers;

import com.google.gson.Gson;

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

	@Override
	public Transfer parse(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, StringTransfer.class);
	}

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
