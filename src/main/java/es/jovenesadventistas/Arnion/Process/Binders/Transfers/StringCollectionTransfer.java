package es.jovenesadventistas.Arnion.Process.Binders.Transfers;

import java.util.Collection;
import com.google.gson.Gson;

public class StringCollectionTransfer implements Transfer {
	private Collection<String> data;
	
	public StringCollectionTransfer(Collection<String> data) {
		this.setData(data);
	}

	public Collection<String> getData() {
		return data;
	}

	public void setData(Collection<String> data) {
		this.data = data;
	}

	@Override
	public Transfer parse(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, StringCollectionTransfer.class);
	}
}
