package es.jovenesadventistas.arnion.process.binders.Transfers;

import java.time.Instant;

import com.google.gson.Gson;

public class StringTransfer implements Transfer {
	private static long __id = 0;
	private long id;
	private long timeStampSeconds;
	
	private String data;
	
	
	public StringTransfer(String data) {
		this.setData(data);
		this.timeStampSeconds = Instant.now().getEpochSecond();
		this.id = ++StringTransfer.__id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
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
		Gson gson = new Gson();
		return gson.fromJson(json, StringTransfer.class);
	}

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
