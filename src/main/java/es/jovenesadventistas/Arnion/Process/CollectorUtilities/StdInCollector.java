package es.jovenesadventistas.Arnion.Process.CollectorUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;

import es.jovenesadventistas.Arnion.Process.Binders.Binder;
import es.jovenesadventistas.Arnion.Process.Binders.Transfers.StreamTransfer;
import es.jovenesadventistas.Arnion.Process.Binders.Transfers.StringTransfer;

/**
 * 
 * @author Adrian E. Sanchez H.
 *
 */
public class StdInCollector {
	private InputStream inputStream, errorStream;
	private ByteSource isByteSource, esByteSource;

	public StdInCollector(Process process) {
		this.inputStream = process.getInputStream();
		this.errorStream = process.getErrorStream();
		this.isByteSource = new ByteSource() {
			@Override
			public InputStream openStream() throws IOException {
				return inputStream;
			}
		};
		this.esByteSource = new ByteSource() {
			@Override
			public InputStream openStream() throws IOException {
				return errorStream;
			}
		};
	}

	public String inputAsCharSource() throws IOException {
		return this.isByteSource.asCharSource(Charsets.UTF_8).read();
	}

	public String errorAsCharSource() throws IOException {
		return this.esByteSource.asCharSource(Charsets.UTF_8).read();
	}

	public void inputCopyTo(OutputStream output) throws IOException {
		this.isByteSource.copyTo(output);
	}

	public void errorCopyTo(OutputStream output) throws IOException {
		this.esByteSource.copyTo(output);
	}

	public StringTransfer getInputAsStringTransfer() throws IOException {
		return new StringTransfer(this.inputAsCharSource());
	}

	public StringTransfer getErrorAsStringTransfer() throws IOException {
		return new StringTransfer(this.errorAsCharSource());
	}
	
	public StreamTransfer getInputAsStreamTransfer() throws IOException {
		return new StreamTransfer(this.inputStream);
	}
	
	public StreamTransfer getErrorAsStreamTransfer() throws IOException {
		return new StreamTransfer(this.errorStream);
	}
}
