package com.atlassearch.atlasSearch;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.SearchIndexModel;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class AtlasSearchApplication {

	@Value("${spring.data.mongodb.uri}")
	private String mongoUri;

	@Value("${spring.data.mongodb.database}")
	private String dbName;

	public static void main(String[] args) {
		SpringApplication.run(AtlasSearchApplication.class, args);
	}

	@Bean
	public CommandLineRunner run() {
		return args -> {
			try (MongoClient mongoClient = MongoClients.create(mongoUri)) {
				MongoDatabase database = mongoClient.getDatabase(dbName);
				MongoCollection<Document> collection = database.getCollection("movies");

				SearchIndexModel indexOne = new SearchIndexModel("testIndex01",
						new Document("mappings",
								new Document("dynamic", true)));

				SearchIndexModel indexTwo = new SearchIndexModel("testIndex02",
						new Document("mappings",
								new Document("dynamic", true).append("fields",
										new Document().append("genres",
												Arrays.asList(
														new Document().append("type", "stringFacet"),
														new Document().append("type", "string")
												)
										).append("year",
												Arrays.asList(
														new Document().append("type", "numberFacet"),
														new Document().append("type", "number")
												)
										))));

				SearchIndexModel indexThree = new SearchIndexModel("testIndex03",
						new Document("mappings",
								new Document("dynamic", false).append("fields",
										new Document().append("fullplot",
														Arrays.asList(
																new Document().append("type", "stringFacet"),
																new Document().append("type", "string"),
																new Document().append("type", "autocomplete")
																		.append("tokenization", "nGram")
																		.append("minGrams", 3)
																		.append("maxGrams", 7)
																		.append("foldDiacritics", false)
														))
												.append("title", new Document().append("type", "string")))
						));

				SearchIndexModel indexFlour = new SearchIndexModel("testIndex04",
						new Document("mappings", new Document()
								.append("dynamic", true)
								.append("fields", new Document()
										.append("fullplot", new Document()
												.append("analyzer", "lucene.english")
												.append("type", "string"))))
								.append("synonyms", List.of(new Document()
										.append("analyzer", "lucene.english")
										.append("name", "synonymName")
										.append("source", new Document()
												.append("collection", "test_synonyms")))));

				// Create indexes
				collection.createSearchIndexes(List.of(indexOne, indexTwo, indexThree, indexFlour));

				// Wait for 4 minutes to wait for indexes to be created.
				Thread.sleep(4 * 60 * 1000);

				// List and delete indexes
				collection.dropSearchIndex("testIndex01");
				collection.dropSearchIndex("testIndex02");
				collection.dropSearchIndex("testIndex03");
				collection.dropSearchIndex("testIndex04");
				System.out.println("Deleted all indexes created");

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.err.println("Thread was interrupted, failed to complete operation");
			}
		};
	}
}