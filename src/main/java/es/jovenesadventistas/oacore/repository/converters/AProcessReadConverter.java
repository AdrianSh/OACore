package es.jovenesadventistas.oacore.repository.converters;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import es.jovenesadventistas.arnion.process.AProcess;

@ReadingConverter
public class AProcessReadConverter implements Converter<Document, AProcess> {

 public AProcess convert(Document source) {
   AProcess p = new AProcess(source.getString("command"));
   p.setId(source.getObjectId("_id"));
   
   return p;
 }
}