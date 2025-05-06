package com.codedifferently.lesson26.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codedifferently.lesson26.library.Librarian;
import com.codedifferently.lesson26.library.Library;
import com.codedifferently.lesson26.library.MediaItem;
import com.codedifferently.lesson26.library.search.SearchCriteria;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@CrossOrigin
public class MediaItemsController {

  private final Library library;
  private final Librarian librarian;

  public MediaItemsController(Library library) throws IOException {
    this.library = library;
    this.librarian = library.getLibrarians().stream().findFirst().orElseThrow();
  }

  @GetMapping("/items")
  public ResponseEntity<GetMediaItemsResponse> getItems() {
    Set<MediaItem> items = library.search(SearchCriteria.builder().build());
    List<MediaItemResponse> responseItems = items.stream().map(MediaItemResponse::from).toList();
    var response = GetMediaItemsResponse.builder().items(responseItems).build();
    return ResponseEntity.ok(response);
  }

  @PostMapping("/items")
 public ResponseEntity<?> addItem(@RequestBody Map<String, Object> requestBody) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode itemNode = mapper.convertValue(requestBody.get("item"), JsonNode.class);

        MediaItem item = MediaItemFactory.fromJson(itemNode);
        library.addItem(item);

        Map<String, Object> response = new HashMap<>();
        response.put("item", item);
        return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("errors", List.of("Invalid item data"));
        return ResponseEntity.badRequest().body(error);
    }
}

  @DeleteMapping("/items/{id}")
public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
    boolean deleted = library.deleteItem(id);
    return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
}
}