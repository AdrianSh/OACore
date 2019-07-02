package es.jovenesadventistas.Arnion.Process.Binders;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.StringTransfer;

public class FileBinder implements Binder<StringTransfer, StringTransfer> {

	public FileBinder() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNext(StringTransfer item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(Throwable throwable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subscribe(Subscriber<? super StringTransfer> subscriber) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean ready() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean joined() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void markAsReady() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void submit(StringTransfer i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
