package com.example.flavorhunt.Listeners;

import com.example.flavorhunt.Models.RecipeDetailsResponse;

public interface RecipeDetailsListener {
    void didFetch(RecipeDetailsResponse response, String message );
    void didError(String message);

}
