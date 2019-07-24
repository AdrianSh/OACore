package es.jovenesadventistas.Arnion.Process.Binders.Transfers;

import java.net.ServerSocket;

import com.google.gson.Gson;

public class ServerSocketTransfer implements Transfer {
	private ServerSocket data;

	public ServerSocketTransfer(ServerSocket data) {
		this.setData(data);
	}

	public ServerSocket getData() {
		return data;
	}

	public void setData(ServerSocket data) {
		this.data = data;
	}
	
	@Override
	public Transfer parse(String json) {
		return new Gson().fromJson(json, ServerSocketTransfer.class);
	}
}
