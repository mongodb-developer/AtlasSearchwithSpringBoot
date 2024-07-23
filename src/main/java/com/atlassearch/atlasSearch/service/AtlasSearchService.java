package com.atlassearch.atlasSearch.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AtlasSearchService {
    private final MongoCollection<Document> collection;
    @Autowired
    private MongoClient mongoClient;

    public AtlasSearchService(MongoTemplate mongoTemplate) {
        MongoDatabase database = mongoTemplate.getDb();
        this.collection = database.getCollection("movies");
    }

    //The testIndex01 sets dynamic mapping set to true and that makes the fields with specific data types automatically indexed.
    public ArrayList<Document> searchMovies(String query) {
        List<Document> pipeline = Arrays.asList(
                new Document("$search",
                        new Document("index", "testIndex01")
                                .append("text",
                                        new Document("query", query)
                                                .append("path",
                                                        new Document("wildcard", "*")))),
                new Document("$project",
                        new Document("_id", 0L)
                                .append("plot", 1L)
                                .append("title", 1L)
                                .append("fullplot", 1L)
        ));

        ArrayList<Document> results = new ArrayList<>();
        collection.aggregate(pipeline).into(results);
        return results;
    }

    //Using testIndex02 to test the field mapping
    public ArrayList<Document> searchMoviesWithGenre(String keyword){
List<Document> pipeline = Arrays.asList(new Document("$search",
                new Document("index", "testIndex02")
                        .append("text",
                                new Document("query", keyword)
                                        .append("path", "genres"))),
        new Document("$project",
                new Document("_id", 0L)
                        .append("title", 1L)
                        .append("genre", 1L)));
        ArrayList<Document> results = new ArrayList<>();
        collection.aggregate(pipeline).into(results);
        return results;
    }

    //using testIndex02 to categorise data received from searchMoviesWithGenre function
    public ArrayList<Document> searchMoviesandCategorise(String keyword){
        List<Document> pipeline = List.of(new Document("$search",
                new Document("index", "testIndex02")
                        .append("facet",
                                new Document("operator",
                                        new Document("compound",
                                                new Document("must", List.of(new Document("text",
                                                        new Document("query", keyword)
                                                                .append("path", Arrays.asList("title", "plot")))))))
                                        .append("facets",
                                                new Document("genresFacet",
                                                        new Document("type", "string")
                                                                .append("path", "genres"))))));
        ArrayList<Document> results = new ArrayList<>();
        collection.aggregate(pipeline).into(results);
        return results;
    }

    //Searching with Autocomplete feature using testIndex03
    public ArrayList<Document> searchWithIncompleteKeyword(String keyword){
      List<Document> pipeline = Arrays.asList(new Document("$search",
                      new Document("index", "testIndex03")
                              .append("autocomplete",
                                      new Document("query", keyword)
                                              .append("path", "fullplot"))),
              new Document("$project",
                      new Document("title", 1L)
                              .append("plot", 1L)
                              .append("fullplot", 1L)));
        ArrayList<Document> results = new ArrayList<>();
        collection.aggregate(pipeline).into(results);
        return results;
    }

    //Fuzzy search using testIndex03
    public ArrayList<Document> searchWithMisspelledTitle(String keyword){
        List<Document> result = Arrays.asList(new Document("$search",
                        new Document("index", "testIndex03")
                                .append("text",
                                        new Document("query", keyword)
                                                .append("path", "title")
                                                .append("fuzzy",
                                                        new Document("maxEdits", 2L)
                                                                .append("maxExpansions", 100L)))),
                new Document("$project",
                        new Document("title", 1L)
                                .append("cast", 1L)));
        return collection.aggregate(result).into(new ArrayList<>());
    }

    //Synonyms search using testIndex04
    public ArrayList<Document> searchWithSynonyms(String keyword){
        List<Document> result = Arrays.asList(new Document("$search",
                        new Document("index", "testIndex04")
                                .append("text",
                                        new Document("path", "fullplot")
                                                .append("query", keyword)
                                                .append("synonyms", "synonymName"))),
                new Document("$limit", 10L),
                new Document("$project",
                        new Document("_id", 0L)
                                .append("title", 1L)
                                .append("fullplot", 1L)
                                .append("score",
                                        new Document("$meta", "searchScore"))));
        return collection.aggregate(result).into(new ArrayList<>());
    }
    public void deleteSearchIndexes(String dbName, String collectionName) {
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
            collection.dropSearchIndex("testIndex01");
            collection.dropSearchIndex("testIndex02");
			collection.dropSearchIndex("testIndex03");
			collection.dropSearchIndex("testIndex04");
			System.out.println("Deleted all indexes created");
        }

}
