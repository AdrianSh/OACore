package es.jovenesadventistas.Arnion;

import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import es.jovenesadventistas.Arnion.Persistence.TransferBatch;
import es.jovenesadventistas.Arnion.Persistence.TransferBatchService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransferBatchTest {
 
    @Autowired
    private TransferBatchService transferBatchService;
 
    @Test
    public void whenApplicationStarts_thenHibernateCreatesInitialRecords() {
        List<TransferBatch> transfers = transferBatchService.list();
 
        Assert.assertEquals(transfers.size(), 3);
    }
}