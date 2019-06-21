package es.ucm.oacore;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import es.jovenesadventistas.Arnion.Process.*;

public class Main {
	public static void main(String[] args) {
		String filesDir = "C:\\Privado\\TFG\\OACORE POC\\src\\main\\java\\es\\ucm\\files\\";
		
		try {
			AProcess p = new SynchProcess("java", "-version");
			p.execute();
			

			/*
			 * 
			 * File workingDir = new File(filesDir);
			 * 
			 * // runProcess("pwd"); System.out.println("**********"); //
			 * runProcess("java -cp target/sample-1.0-SNAPSHOT.jar es.ucm.oacore.commands.App"
			 * , new File("C:\\Privado\\TFG\\OACORE POC\\commands\\sample")); //
			 * runProcess("javac -cp es/ucm/files Test.java", workingDir); //
			 * runProcess("java -cp . Test", workingDir); System.out.println("**********");
			 * // runProcess("jar -cf Test.jar Test.class", workingDir); //
			 * runProcess("java -jar Test.jar testando", workingDir);
			 * 
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void createAMVNDir() throws Exception {
		runProcess(
				"mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false",
				new File("../commands/test/"));
	}

	private static void printLines(String cmd, InputStream ins) throws Exception {
		String line = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(ins));
		while ((line = in.readLine()) != null) {
			System.out.println(cmd + " " + line);
		}
	}

	private static void runProcess(String command, File file) throws Exception {
		Process pro = Runtime.getRuntime().exec(command, null, file);
		printLines(command + " stdout:", pro.getInputStream());
		printLines(command + " stderr:", pro.getErrorStream());
		pro.waitFor();
		System.out.println(command + " exitValue() " + pro.exitValue());
	}

}
