package Recipe.Hub.repository;

import Recipe.Hub.model.Favorite;
import Recipe.Hub.model.Recipe;
import Recipe.Hub.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository
        extends JpaRepository<Favorite, Long> {

    Favorite findByUserAndRecipe(
            User user,
            Recipe recipe
    );

    List<Favorite> findByUser(User user);
}