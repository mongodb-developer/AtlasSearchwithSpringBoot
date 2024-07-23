package com.atlassearch.atlasSearch.controller;

import com.atlassearch.atlasSearch.service.AtlasSearchService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class MovieController {

    @Autowired
    private AtlasSearchService movieService;

    @GetMapping("/search")
    public ArrayList<Document> searchMovies(@RequestParam String query) {
        return movieService.searchMovies(query);
    }

    @GetMapping("/searchMoviesWithGenre")
    public ArrayList<Document> searchMoviesWithGenre(@RequestParam String query) {
        return movieService.searchMoviesWithGenre(query);
    }

    @GetMapping("/searchMovies")
    public ArrayList<Document> searchMoviesandCategorise(@RequestParam String query) {
        return movieService.searchMoviesandCategorise(query);
    }
    @GetMapping("/searchMoviesWithAutocomplete")
    public ArrayList<Document> searchWithIncompleteKeyword(@RequestParam String query) {
        return movieService.searchWithIncompleteKeyword(query);
    }
    @GetMapping("/searchMoviesWithIncorrectSpelling")
    public ArrayList<Document> searchWithMisspelledTitle(@RequestParam String query) {
        return movieService.searchWithMisspelledTitle(query);
    }
    @GetMapping("/searchMoviesWithSynonyms")
    public ArrayList<Document> searchWithSynonyms(@RequestParam String query) {
        return movieService.searchWithSynonyms(query);
    }

    @DeleteMapping("/deleteSearchIndexes")
    public void deleteSearchIndexes(@RequestParam String dbName, @RequestParam String collectionName) {
        movieService.deleteSearchIndexes(dbName, collectionName);
    }
}


