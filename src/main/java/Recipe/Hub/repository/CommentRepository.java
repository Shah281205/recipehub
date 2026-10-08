package Recipe.Hub.repository;

import Recipe.Hub.model.Comment;
import Recipe.Hub.model.Recipe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByRecipe(Recipe recipe);
}