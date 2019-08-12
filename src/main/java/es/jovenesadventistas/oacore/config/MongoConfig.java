package es.jovenesadventistas.oacore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;


@Configuration
@EnableMongoRepositories(basePackages = "es.jovenesadventistas")
public class MongoConfig extends AbstractMongoConfiguration {
	@Value("${es.jovenesadventistas.oacore.mongodb.database}")
	private String dbName;
	
	@Value("${es.jovenesadventistas.oacore.mongodb.host}")
	private String dbHost;
	
	@Value("${es.jovenesadventistas.oacore.mongodb.port}")
	private int dbPort;
	
    @Override
    protected String getDatabaseName() {
        return dbName;
    }
  
    @Override
    public MongoClient mongoClient() {
        return new MongoClient(dbHost, dbPort);
    }
  
    @Override
    protected String getMappingBasePackage() {
        return "es.jovenesadventistas";
    }
}
