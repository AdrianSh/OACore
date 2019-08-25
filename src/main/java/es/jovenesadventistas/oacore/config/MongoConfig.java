package es.jovenesadventistas.oacore.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;

import es.jovenesadventistas.oacore.repository.converters.AProcessConverter;
import es.jovenesadventistas.oacore.repository.converters.APublisherConverter;
import es.jovenesadventistas.oacore.repository.converters.ASubscriberConverter;
import es.jovenesadventistas.oacore.repository.converters.BinderConverter;
import es.jovenesadventistas.oacore.repository.converters.WorkflowConverter;


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
    
    @Bean
    @Override
    public MongoCustomConversions customConversions() {
      List<Converter<?, ?>> converterList = new ArrayList<Converter<?, ?>>();
      converterList.add(new AProcessConverter.AProcessReadConverter());
      converterList.add(new AProcessConverter.AProcessWriteConverter());
      converterList.add(new WorkflowConverter.WorkflowReadConverter());
      converterList.add(new WorkflowConverter.WorkflowWriteConverter());
      converterList.add(new BinderConverter.BinderReadConverter());
      converterList.add(new BinderConverter.BinderWriteConverter());
      converterList.add(new APublisherConverter.APublisherReadConverter());
      converterList.add(new APublisherConverter.APublisherWriteConverter());
      converterList.add(new ASubscriberConverter.ASubscriberReadConverter());
      converterList.add(new ASubscriberConverter.ASubscriberWriteConverter());
      return new MongoCustomConversions(converterList);
    }
}
