package es.jovenesadventistas.arnion.process.persistence;

import java.util.Collection;
import java.util.LinkedList;

import es.jovenesadventistas.arnion.process.binders.transfers.Transfer;

public class TransferStore<E extends Transfer> extends LinkedList<E> {
	private static final long serialVersionUID = 2134842386414451013L;
	private TransferService tService;
	
	public TransferStore() {
		super();
		tService = TransferService.getInstance();
	}
	
	public TransferStore(Collection<? extends E> c) {
		super(c);
		tService = TransferService.getInstance();
	}
	
	@Override
	public boolean add(E e) {
		tService.insert(e);
		return super.add(e);
	}
	
	@Override
	public void clear() {
		this.forEach(t -> {
			tService.delete(t);
		});
		super.clear();
	}
	
	@Override
	public E remove() {
		E t = super.remove();
		tService.delete(t);
		return t;
	}

	// Pending to override all functions with the service
}
