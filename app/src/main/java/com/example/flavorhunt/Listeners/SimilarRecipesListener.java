package com.example.flavorhunt.Listeners;

import com.example.flavorhunt.Models.SimilarRecipeResponse;
import java.util.List;

public interface SimilarRecipesListener {
    void didFetch(List<SimilarRecipeResponse> response, String message);
    void didError(String message);

}
