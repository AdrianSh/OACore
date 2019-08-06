package es.jovenesadventistas.Arnion.Process.Binders.Transfers;

import java.net.Socket;
import java.time.Instant;

import com.google.gson.Gson;

public class SocketTransfer implements Transfer {
	private static long __id = 0;
	private long id;
	private long timeStampSeconds;
	private Socket data;

	public SocketTransfer(Socket data) {
		this.setData(data);
		this.timeStampSeconds = Instant.now().getEpochSecond();
		this.id = ++SocketTransfer.__id;
	}

	public Socket getData() {
		return data;
	}

	public void setData(Socket data) {
		this.data = data;
	}

	public long getTimeStampSeconds() {
		return this.timeStampSeconds;
	}
	
	public long getId() {
		return id;
	}

	@Override
	public Transfer parse(String json) {
		return new Gson().fromJson(json, SocketTransfer.class);
	}
}
