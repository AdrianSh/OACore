package es.jovenesadventistas.oacore.repository.converters;

import java.io.File;
import java.util.HashMap;
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
			document.put("workingdirectory",
					source.getWorkingDirectory() != null ? source.getWorkingDirectory().getPath() : null);
			document.put("modifiedEnvironment", gson.toJson(source.getModifiedEnvironment()));
			document.put("inheritIO", source.isInheritIO());
			document.put("userId", source.getUserId());
			return document;
		}
	}

	@ReadingConverter
	public static class AProcessReadConverter implements Converter<Document, AProcess> {
		private Gson gson = new Gson();

		@SuppressWarnings("unchecked")
		public AProcess convert(Document source) {
			String command = source.getString("command").trim();
			AProcess p = new AProcess(command);
			p.setId(source.getObjectId("_id"));
			String workingDirectory = source.getString("workingdirectory");
			
			if (workingDirectory != null && workingDirectory.length() > 0)
				p.setWorkingDirectory(new File(workingDirectory));

			Object mEnvObj = source.get("modifiedEnvironment");
			HashMap<String, String> modEnvironment = new HashMap<String, String>();
			
			if (mEnvObj instanceof Document) {
				Document modEnv = (Document) mEnvObj;
				modEnv.forEach((key, value) -> {
					if (key != null && key.length() > 0 || value != null && value.toString().length() > 0)
						modEnvironment.put(key, value.toString());
				});

				if (modEnvironment != null && modEnvironment.size() > 0)
					p.setModifiedEnvironment(modEnvironment);
			} else {
				p.setModifiedEnvironment(gson.fromJson(mEnvObj.toString(), new HashMap<String, String>().getClass()));
			}

			p.setInheritIO(source.getBoolean("inheritIO", false));
			p.setUserId(source.getObjectId("userId"));
			return p;
		}
	}
}