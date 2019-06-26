package es.jovenesadventistas.Arnion.Process.Binders;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;

public class ExitCodeBinder<T extends Transfer, S extends Transfer> implements Binder<T, S> {

	public ExitCodeBinder() {
		
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		
	}

	@Override
	public void onNext(T item) {
		
	}

	@Override
	public void onError(Throwable throwable) {
		
	}

	@Override
	public void onComplete() {
		
	}

	@Override
	public void subscribe(Subscriber<? super S> subscriber) {
		
	}

	@Override
	public boolean ready() {
		// TODO Auto-generated method stub
		return false;
	}
}
