package es.jovenesadventistas.arnion.process.binders.Transfers;

import java.io.InputStream;
import java.time.Instant;
import java.util.Map;

import com.google.gson.Gson;

public class StreamTransfer implements Transfer {
	private static long __id = 0;
	private long id;
	private long timeStampSeconds;
	private InputStream inputStream, errorStream;

	public StreamTransfer(InputStream inputStream, InputStream errorStream) {
		this.inputStream = inputStream;
		this.errorStream = errorStream;
		this.timeStampSeconds = Instant.now().getEpochSecond();
		this.id = ++StreamTransfer.__id;
	}

	public StreamTransfer(InputStream stream) {
		this.inputStream = stream;
		this.timeStampSeconds = Instant.now().getEpochSecond();
		this.id = ++StreamTransfer.__id;
	}

	public Map.Entry<InputStream, InputStream> getData() {
		return new Map.Entry<InputStream, InputStream>() {
			@Override
			public InputStream getKey() {
				return inputStream;
			}

			@Override
			public InputStream getValue() {
				return errorStream;
			}

			@Override
			public InputStream setValue(InputStream value) {
				return null;
			}
		};
	}

	public void setData(InputStream inputStream, InputStream errorStream) {
		this.inputStream = inputStream;
		this.errorStream = errorStream;
	}

	public void setData(InputStream stream) {
		this.inputStream = stream;
	}

	public long getTimeStampSeconds() {
		return this.timeStampSeconds;
	}
	
	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "StreamTransfer [inputStream=" + inputStream + ", errorStream=" + errorStream + "]";
	}

	@Override
	public Transfer parse(String json) {
		return new Gson().fromJson(json, StreamTransfer.class);
	}
}
