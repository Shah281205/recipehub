package Recipe.Hub.repository;

import Recipe.Hub.model.Recipe;
import Recipe.Hub.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByUser(User user);

    List<Recipe> findByStatusAndTitleContainingIgnoreCase(
            String status,
            String title
    );

    List<Recipe> findByStatusAndIngredientsContainingIgnoreCase(
            String status,
            String ingredients
    );

}