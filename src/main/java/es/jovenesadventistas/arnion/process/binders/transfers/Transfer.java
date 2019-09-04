package es.jovenesadventistas.arnion.process.binders.transfers;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public interface Transfer {
	public Transfer parse(String json);
}
