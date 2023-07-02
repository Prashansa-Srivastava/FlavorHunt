package com.example.flavorhunt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flavorhunt.Adapters.IngredientsAdapter;
import com.example.flavorhunt.Adapters.InstructionsAdapter;
import com.example.flavorhunt.Adapters.SimilarRecipeAdapter;
import com.example.flavorhunt.Listeners.InstructionsListener;
import com.example.flavorhunt.Listeners.RecipeClickListener;
import com.example.flavorhunt.Listeners.RecipeDetailsListener;
import com.example.flavorhunt.Listeners.SimilarRecipesListener;
import com.example.flavorhunt.Models.InstructionsResponse;
import com.example.flavorhunt.Models.RecipeDetailsResponse;
import com.example.flavorhunt.Models.SimilarRecipeResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RecipeDetailsActivity extends AppCompatActivity {

    int id;
    TextView textView_meal_name,textView_meal_summary,textView_meal_source;
    ImageView imageView_meal_image;
    RecyclerView recycler_meal_ingredients, recycler_meal_similar,recycler_meal_instruction;
    RequestManager manager;

    FirebaseAuth auth;
    FirebaseFirestore db;
    ProgressDialog dialog;
    IngredientsAdapter ingredientsAdapter;
    SimilarRecipeAdapter similarRecipeAdapter;
    InstructionsAdapter instructionsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        findViews();

        id = Integer.parseInt(getIntent().getStringExtra("id"));
        manager = new RequestManager(this);
        manager.getRecipeDetails(recipeDetailsListener,id);
        manager.getSimilarRecipes(similarRecipesListener,id);
        manager.getInstructions(instructionsListener,id);
        dialog = new ProgressDialog(this);
        dialog.setTitle("LOADING DETAILS......");
        dialog.show();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String recipeId = getIntent().getStringExtra("recipeId");

        trackRecipeView(recipeId);


    }

    private void trackRecipeView(String recipeId) {
        // Get the current user's unique identifier (e.g., UID)
        String userId = auth.getCurrentUser().getUid();

        // Create a document reference with the user's unique identifier in the "users" collection
        DocumentReference userDocRef = db.collection("users").document(userId);

        // Retrieve the current recipe view count value from Firestore
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        long currentViewCount = document.getLong("recipeViewCount");
                        long updatedViewCount = currentViewCount + 1;

                        // Update the recipeViewCount field with the incremented value
                        userDocRef.update("recipeViewCount", updatedViewCount,
                                        "payment", "inactive")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Recipe view count updated successfully
                                            if(updatedViewCount == 3){
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent intent = new Intent(RecipeDetailsActivity.this,PaymentActivity.class);
                                                        startActivity(intent);
                                                    }
                                                },4000);

                                            }
                                        } else {
                                            // Error updating recipe view count
                                            Toast.makeText(RecipeDetailsActivity.this, "Error updating recipe view count: " + task.getException(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        // Document doesn't exist
                        Toast.makeText(RecipeDetailsActivity.this, "User document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Error retrieving document
                    Toast.makeText(RecipeDetailsActivity.this, "Error retrieving user document: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    private void findViews() {
        textView_meal_name = findViewById(R.id.textView_meal_name);
        textView_meal_summary = findViewById(R.id.textView_meal_summary);
        textView_meal_source = findViewById(R.id.textView_meal_source);
        imageView_meal_image = findViewById(R.id.imageView_meal_image);
        recycler_meal_ingredients = findViewById(R.id.recycler_meal_ingredients);
        recycler_meal_similar = findViewById(R.id.recycler_meal_similar);
        recycler_meal_instruction = findViewById(R.id.recycler_meal_instruction);

    }
    private final RecipeDetailsListener recipeDetailsListener = new RecipeDetailsListener() {
        @Override
        public void didFetch(RecipeDetailsResponse response, String message) {
            dialog.dismiss();
            textView_meal_name.setText(response.title );
            textView_meal_source.setText(response.sourceName);
            textView_meal_summary.setText(response.summary);
            Picasso.get().load(response.image).into(imageView_meal_image);

            recycler_meal_ingredients.setHasFixedSize(true);
            recycler_meal_ingredients.setLayoutManager(new LinearLayoutManager(RecipeDetailsActivity.this,LinearLayoutManager.HORIZONTAL,false));
            ingredientsAdapter = new IngredientsAdapter(RecipeDetailsActivity.this,response.extendedIngredients);
            recycler_meal_ingredients.setAdapter(ingredientsAdapter);
        }

        @Override
        public void didError(String message) {
            Toast.makeText(RecipeDetailsActivity.this, "message ", Toast.LENGTH_SHORT).show();

        }
    };
    private final SimilarRecipesListener similarRecipesListener = new SimilarRecipesListener() {
        @Override
        public void didFetch(List<SimilarRecipeResponse> response, String message) {
            recycler_meal_similar.setHasFixedSize(true);
            recycler_meal_similar.setLayoutManager(new LinearLayoutManager(RecipeDetailsActivity.this,LinearLayoutManager.HORIZONTAL,false));
            similarRecipeAdapter = new SimilarRecipeAdapter(RecipeDetailsActivity.this,response,recipeClickListener);
            recycler_meal_similar.setAdapter(similarRecipeAdapter);
        }

        @Override
        public void didError(String message) {
            Toast.makeText(RecipeDetailsActivity.this,message,Toast.LENGTH_SHORT).show();

        }
    };
    private final RecipeClickListener recipeClickListener = new RecipeClickListener() {
        @Override
        public void onRecipeClicked(String id) {
            //Toast.makeText(RecipeDetailsActivity.this,id,Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RecipeDetailsActivity.this,RecipeDetailsActivity.class)
                    .putExtra("id" , id));

        }
    };
    private  final InstructionsListener instructionsListener = new InstructionsListener() {
        @Override
        public void didFetch(List<InstructionsResponse> response, String message) {
            recycler_meal_instruction.setHasFixedSize(true);
            recycler_meal_instruction.setLayoutManager(new LinearLayoutManager(RecipeDetailsActivity.this, LinearLayoutManager.VERTICAL,false));
            instructionsAdapter = new InstructionsAdapter(RecipeDetailsActivity.this,response);
            recycler_meal_instruction.setAdapter(instructionsAdapter);

        }

        @Override
        public void didError(String message) {

        }
    };


}