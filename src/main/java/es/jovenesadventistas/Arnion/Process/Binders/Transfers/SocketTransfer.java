package es.jovenesadventistas.Arnion.Process.Binders.Transfers;

import java.net.Socket;

import com.google.gson.Gson;

public class SocketTransfer implements Transfer {
	private Socket data;

	public SocketTransfer(Socket data) {
		this.setData(data);
	}

	public Socket getData() {
		return data;
	}

	public void setData(Socket data) {
		this.data = data;
	}
	
	@Override
	public Transfer parse(String json) {
		return new Gson().fromJson(json, SocketTransfer.class);
	}
}
