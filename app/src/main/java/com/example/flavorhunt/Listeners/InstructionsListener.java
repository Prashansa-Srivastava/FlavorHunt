package com.example.flavorhunt.Listeners;

import com.example.flavorhunt.Models.InstructionsResponse;

import java.util.List;

public interface InstructionsListener {
    void didFetch(List<InstructionsResponse> response, String message);
    void didError(String message);
}
