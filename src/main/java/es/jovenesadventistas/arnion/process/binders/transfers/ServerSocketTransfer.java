package es.jovenesadventistas.arnion.process.binders.transfers;

import java.net.ServerSocket;
import java.time.Instant;

import com.google.gson.Gson;

public class ServerSocketTransfer implements Transfer {
	private static long __id = 0;
	private long id;
	private long timeStampSeconds;

	private ServerSocket data;

	public ServerSocketTransfer(ServerSocket data) {
		this.setData(data);
		this.timeStampSeconds = Instant.now().getEpochSecond();
		this.id = ++ServerSocketTransfer.__id;
	}

	public ServerSocket getData() {
		return data;
	}

	public void setData(ServerSocket data) {
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
		return new Gson().fromJson(json, ServerSocketTransfer.class);
	}
}
