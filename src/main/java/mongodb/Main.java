package mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class Main {
  
  public static List<Recipe> recipes = Arrays.asList(
      new Recipe("elotes",
              Arrays.asList("corn", "mayonnaise", "cotija cheese", "sour cream", "lime" ),
              35),
      new Recipe("loco moco",
              Arrays.asList("ground beef", "butter", "onion", "egg", "bread bun", "mushrooms" ),
              54),
      new Recipe("patatas bravas",
              Arrays.asList("potato", "tomato", "olive oil", "onion", "garlic", "paprika" ),
              80),
      new Recipe("fried rice",
              Arrays.asList("rice", "soy sauce", "egg", "onion", "pea", "carrot", "sesame oil" ),
              40)
  );

  public static void main(String[] args) {
    Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.WARNING);
    ConnectionString mongoUri = new ConnectionString("mongodb+srv://lalitkumarsaini1994:Password%40893@cluster0.pnkl8.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");

    String dbName = "myDatabase";
    String collectionName = "recipes";

    CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    MongoClientSettings settings = MongoClientSettings.builder()
            .codecRegistry(pojoCodecRegistry)
            .applyConnectionString(mongoUri).build();

    MongoClient mongoClient = null;
    try {
       mongoClient = MongoClients.create(settings);
    } catch (MongoException me) {
      System.err.println("Unable to connect to the MongoDB instance due to an error: " + me);
      System.exit(1);
    }

    MongoDatabase database = mongoClient.getDatabase(dbName);
    MongoCollection<Recipe> collection = database.getCollection(collectionName, Recipe.class);

    

    try {
      
      InsertManyResult result = collection.insertMany(recipes);
      System.out.println("Inserted " + result.getInsertedIds().size() + " documents.\n");
    } catch (MongoException me) {
      System.err.println("Unable to insert any recipes into MongoDB due to an error: " + me);
      System.exit(1);
    }

    

    try (MongoCursor<Recipe> cursor = collection.find().iterator()) {
      while (cursor.hasNext()) {
        Recipe currentRecipe = cursor.next();
        System.out.printf("%s has %d ingredients and takes %d minutes to make\n", currentRecipe.getName(), currentRecipe.getIngredients().size(), currentRecipe.getPrepTimeInMinutes());
      }
    } catch (MongoException me) {
      System.err.println("Unable to find any recipes in MongoDB due to an error: " + me);
    }

    
    
    
    

    Bson findPotato = Filters.eq("ingredients", "potato");
    try {
      Recipe firstPotato = collection.find(findPotato).first();
      if (firstPotato == null) {
        System.out.println("Couldn't find any recipes containing 'potato' as an ingredient in MongoDB.");
        System.exit(1);
      }
    } catch (MongoException me) {
      System.err.println("Unable to find a recipe to update in MongoDB due to an error: " + me);
      System.exit(1);
    }

    
    Bson updateFilter = Updates.set("prepTimeInMinutes", 72);

    
    
    
    FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);

    
    
    try {
      Recipe updatedDocument = collection.findOneAndUpdate(findPotato,
              updateFilter, options);
      if (updatedDocument == null) {
        System.out.println("Couldn't update the recipe. Did someone (or something) delete it?");
      } else {
        System.out.println("\nUpdated the recipe to: " + updatedDocument);
      }
    } catch (MongoException me) {
      System.err.println("Unable to update any recipes due to an error: " + me);
    }

    
    Bson deleteFilter = Filters.in("name", Arrays.asList("elotes", "fried rice"));
    try {
      DeleteResult deleteResult = collection
              .deleteMany(deleteFilter);
      System.out.printf("\nDeleted %d documents.\n", deleteResult.getDeletedCount());
    } catch (MongoException me) {
      System.err.println("Unable to delete any recipes due to an error: " + me);
    }

    
    mongoClient.close();
  }

  
  
  public static class Recipe {
    private String name;
    private List<String> ingredients;
    private int prepTimeInMinutes;

    public Recipe(String name, List<String> ingredients, int prepTimeInMinutes) {
      this.name = name;
      this.ingredients = ingredients;
      this.prepTimeInMinutes = prepTimeInMinutes;
    }

    
    
    public Recipe() {
      ingredients = new ArrayList<String>();
      name = "";
    }

    @Override
    public String toString() {
      final StringBuffer sb = new StringBuffer("Recipe{");
      sb.append("name=").append(name);
      sb.append(", ingredients=").append(ingredients);
      sb.append(", prepTimeInMinutes=").append(prepTimeInMinutes);
      sb.append('}');
      return sb.toString();
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public List<String> getIngredients() {
      return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
      this.ingredients = ingredients;
    }

    public int getPrepTimeInMinutes() {
      return prepTimeInMinutes;
    }

    public void setPrepTimeInMinutes(int prepTimeInMinutes) {
      this.prepTimeInMinutes = prepTimeInMinutes;
    }
  }
}
