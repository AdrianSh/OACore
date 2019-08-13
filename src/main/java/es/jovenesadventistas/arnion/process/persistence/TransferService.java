package es.jovenesadventistas.arnion.process.persistence;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import es.jovenesadventistas.arnion.process.binders.Transfers.Transfer;

public class TransferService {
	private static TransferService instance;
	private MongoClient mongoClient;
	private MongoDatabase database;
	
	public static TransferService getInstance() {
		if(instance == null)
			instance = new TransferService();
		return instance;
	}
	
	private TransferService() {
		mongoClient = new MongoClient("localhost", 27017);
		database = mongoClient.getDatabase("Transfers");
	}
	
	public void insert(Transfer t) {
		MongoCollection<Document> collection = database.getCollection(t.getClass().getName());
		Document doc = Document.parse(t.toString()).append("_id", t.hashCode());
		collection.insertOne(doc);
	}
	
	public UpdateResult update(Transfer t) {
		MongoCollection<Document> collection = database.getCollection(t.getClass().getName());
		return collection.updateOne(new Document().append("_id", t.hashCode()), Document.parse(t.toString()));
	}
	
	public FindIterable<Document> find(Transfer t) {
		MongoCollection<Document> collection = database.getCollection(t.getClass().getName());
		return collection.find(new Document().append("_id", t.hashCode()));
	}
	
	public DeleteResult delete(Transfer t) {
		MongoCollection<Document> collection = database.getCollection(t.getClass().getName());
		return collection.deleteOne(new Document().append("_id", t.hashCode()));
	}
	
	public void close() {
		mongoClient.close();
	}
}
