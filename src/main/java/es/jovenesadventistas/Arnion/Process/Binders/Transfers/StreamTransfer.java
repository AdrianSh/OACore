package es.jovenesadventistas.Arnion.Process.Binders.Transfers;

import java.io.InputStream;
import java.util.Map;

public class StreamTransfer implements Transfer {
	private InputStream inputStream, errorStream;

	public StreamTransfer(InputStream inputStream, InputStream errorStream) {
		this.inputStream = inputStream;
		this.errorStream = errorStream;
	}

	public StreamTransfer(InputStream stream) {
		this.inputStream = stream;
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

	@Override
	public String toString() {
		return "StreamTransfer [inputStream=" + inputStream + ", errorStream=" + errorStream + "]";
	}
}
