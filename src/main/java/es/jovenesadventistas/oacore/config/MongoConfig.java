package es.jovenesadventistas.oacore.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;

import es.jovenesadventistas.oacore.repository.converters.AProcessReadConverter;
import es.jovenesadventistas.oacore.repository.converters.AProcessWriteConverter;


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
    
    // MappingMongoConverter 
    @Bean
    @Override
    public MongoCustomConversions customConversions() {
      List<Converter<?, ?>> converterList = new ArrayList<Converter<?, ?>>();
      converterList.add(new AProcessReadConverter());
      converterList.add(new AProcessWriteConverter());
      return new MongoCustomConversions(converterList);
    }
}
