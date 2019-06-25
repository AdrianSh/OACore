package es.jovenesadventistas.Arnion.Process.Binders;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Future;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;

public interface Binder<T extends Transfer> extends Subscriber<T> {
	
}
