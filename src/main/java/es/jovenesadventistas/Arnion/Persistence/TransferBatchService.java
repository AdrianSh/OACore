package es.jovenesadventistas.Arnion.Persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransferBatchService {
	@Autowired
    private TransferBatchRepository transferBatchRepository;
 
    public List<TransferBatch> list() {
        return transferBatchRepository.findAll();
    }
    
    public TransferBatch save(TransferBatch t) {
    	return transferBatchRepository.save(t);
    }
}
