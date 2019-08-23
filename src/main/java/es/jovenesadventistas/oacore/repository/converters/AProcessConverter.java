package es.jovenesadventistas.oacore.repository.converters;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import com.google.gson.Gson;

import es.jovenesadventistas.arnion.process.AProcess;

public class AProcessConverter {
	@WritingConverter
	public static class AProcessWriteConverter implements Converter<AProcess, Document> {
		private Gson gson = new Gson();
		
		public Document convert(AProcess source) {
			Document document = new Document();
			document.put("_id", source.getId());
			String command = "";
			for (String c : source.getCommand()) {
				command += c + " ";
			}

			document.put("command", command);
			document.put("workingdirectory", source.getWorkingDirectory() != null ? source.getWorkingDirectory().getPath() : null);
			document.put("modifiedEnvironment", gson.toJson(source.getModifiedEnvironment()));
			document.put("inheritIO", source.isInheritIO());
			document.put("userId", source.getUserId());
			return document;
		}
	}
	
	@ReadingConverter
	public static class AProcessReadConverter implements Converter<Document, AProcess> {
		private Gson gson = new Gson();
		
		public AProcess convert(Document source) {
			AProcess p = new AProcess(source.getString("command"));
			p.setId(source.getObjectId("_id"));
			String workingDirectory = source.getString("workingdirectory");
			if(workingDirectory != null && workingDirectory.length() > 0) p.setWorkingDirectory(new File(workingDirectory));
			@SuppressWarnings("unchecked")
			Map<String, String> o = gson.fromJson(source.getString("modifiedEnvironment"), new HashMap<String, String>().getClass());
			if(o != null && o.size() > 0) p.setModifiedEnvironment(o);
			p.setInheritIO(source.getBoolean("inheritIO", false));
			p.setUserId(source.getObjectId("userId"));
			return p;
		}
	}
}