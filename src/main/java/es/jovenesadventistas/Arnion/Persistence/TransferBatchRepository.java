package es.jovenesadventistas.Arnion.Persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferBatchRepository extends JpaRepository<TransferBatch, Long> {

}
