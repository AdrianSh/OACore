package es.jovenesadventistas.Arnion.Process.Binders.Transfers;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;

public class IntegerTransfer implements Transfer {
	@SuppressWarnings("unused")
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	
	private AtomicInteger data;
	
	public IntegerTransfer() {
		this.data = new AtomicInteger();
	}
	
	public IntegerTransfer(Integer data) {
		this.data = new AtomicInteger(data);
	}

	public AtomicInteger getData() {
		return data;
	}

	public void setData(Integer data) {
		this.data.set(data);
	}
	
	@Override
	public Transfer parse(String json) {
		return new Gson().fromJson(json, IntegerTransfer.class);
	}

}
