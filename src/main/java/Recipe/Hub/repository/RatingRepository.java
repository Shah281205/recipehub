package Recipe.Hub.repository;

import Recipe.Hub.model.Rating;
import Recipe.Hub.model.Recipe;
import Recipe.Hub.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByRecipe(Recipe recipe);

    Rating findByUserAndRecipe(User user, Recipe recipe);

}