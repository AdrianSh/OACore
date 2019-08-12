package es.jovenesadventistas.arnion.process.binders.Transfers;

import java.time.Instant;
import java.util.Collection;
import com.google.gson.Gson;

public class StringCollectionTransfer implements Transfer {
	private static long __id = 0;
	private long id;
	private long timeStampSeconds;
	private Collection<String> data;
	
	public StringCollectionTransfer(Collection<String> data) {
		this.setData(data);
		this.timeStampSeconds = Instant.now().getEpochSecond();
		this.id = ++StringCollectionTransfer.__id;
	}

	public Collection<String> getData() {
		return data;
	}

	public void setData(Collection<String> data) {
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
		return gson.fromJson(json, StringCollectionTransfer.class);
	}
}
