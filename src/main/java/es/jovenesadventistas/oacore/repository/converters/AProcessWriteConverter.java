package es.jovenesadventistas.oacore.repository.converters;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import es.jovenesadventistas.arnion.process.AProcess;

@WritingConverter
public class AProcessWriteConverter implements Converter<AProcess, Document> {

  public Document convert(AProcess source) {
    Document document = new Document();
    document.put("_id", source.getId());
    
    String command = "";
    for (String c : source.getCommand()) {
    	command += c + " ";
	}
    
    document.put("command", command);
    
    return document;
  }
}