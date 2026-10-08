package Recipe.Hub.controller;

import Recipe.Hub.model.Recipe;
import Recipe.Hub.model.User;
import Recipe.Hub.model.Comment;
import Recipe.Hub.model.Rating;
import Recipe.Hub.model.SystemSetting;
import Recipe.Hub.model.Favorite;

import Recipe.Hub.repository.RecipeRepository;
import Recipe.Hub.repository.CommentRepository;
import Recipe.Hub.repository.RatingRepository;
import Recipe.Hub.repository.SystemSettingRepository;
import Recipe.Hub.repository.FavoriteRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final FavoriteRepository favoriteRepository;

    public RecipeController(
            RecipeRepository recipeRepository,
            CommentRepository commentRepository,
            RatingRepository ratingRepository,
            SystemSettingRepository systemSettingRepository,
            FavoriteRepository favoriteRepository) {

        this.recipeRepository = recipeRepository;
        this.commentRepository = commentRepository;
        this.ratingRepository = ratingRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @GetMapping("/add-recipe")
    public String addRecipe(HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        return "add-recipe";
    }


    @PostMapping("/add-recipe")
    public String saveRecipe(
            @RequestParam String title,
            @RequestParam String ingredients,
            @RequestParam String instructions,
            @RequestParam(required = false) String imageUrl,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Recipe recipe = new Recipe();

        recipe.setTitle(title);
        recipe.setIngredients(ingredients);
        recipe.setInstructions(instructions);
        recipe.setImageUrl(imageUrl);
        recipe.setUser(user);

        SystemSetting setting =
                systemSettingRepository.findFirstByOrderByIdAsc();

        if (setting != null &&
                "Automatic Approval".equalsIgnoreCase(
                        setting.getRecipeApproval())) {

            recipe.setStatus("APPROVED");

        } else {

            recipe.setStatus("PENDING");
        }

        recipeRepository.save(recipe);

        return "redirect:/my-recipes";
    }


    @GetMapping("/recipes")
    public String showRecipes(
            @RequestParam(required = false, defaultValue = "") String search,
            Model model,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        List<Recipe> allRecipes;

        if (search == null || search.trim().isEmpty()) {

            allRecipes = recipeRepository.findAll();

        } else {

            List<Recipe> titleResults =
                    recipeRepository
                            .findByStatusAndTitleContainingIgnoreCase(
                                    "APPROVED",
                                    search.trim());

            List<Recipe> ingredientResults =
                    recipeRepository
                            .findByStatusAndIngredientsContainingIgnoreCase(
                                    "APPROVED",
                                    search.trim());

            allRecipes = new ArrayList<>();

            allRecipes.addAll(titleResults);

            for (Recipe recipe : ingredientResults) {

                if (!allRecipes.contains(recipe)) {
                    allRecipes.add(recipe);
                }
            }
        }

        List<Recipe> approvedRecipes =
                allRecipes.stream()
                        .filter(recipe ->
                                "APPROVED".equalsIgnoreCase(
                                        recipe.getStatus()))
                        .toList();


        Map<Long, Double> averageRatings = new HashMap<>();

        Map<Long, Integer> totalRatings = new HashMap<>();

        for (Recipe recipe : approvedRecipes) {

            List<Rating> ratings =
                    ratingRepository.findByRecipe(recipe);

            int total = ratings.size();

            double average = 0;

            if (total > 0) {

                int sum = 0;

                for (Rating rating : ratings) {
                    sum += rating.getValue();
                }

                average = (double) sum / total;
            }

            averageRatings.put(recipe.getId(), average);

            totalRatings.put(recipe.getId(), total);
        }


        Map<Long, Boolean> favoriteStatus = new HashMap<>();

        for (Recipe recipe : approvedRecipes) {

            Favorite favorite =
                    favoriteRepository.findByUserAndRecipe(
                            user,
                            recipe);

            favoriteStatus.put(
                    recipe.getId(),
                    favorite != null);
        }


        SystemSetting setting =
                systemSettingRepository.findFirstByOrderByIdAsc();

        boolean commentsEnabled = true;

        boolean ratingsEnabled = true;

        if (setting != null) {

            commentsEnabled =
                    "Comments Enabled".equalsIgnoreCase(
                            setting.getComments());

            ratingsEnabled =
                    "Ratings Enabled".equalsIgnoreCase(
                            setting.getRatings());
        }


        model.addAttribute(
                "recipes",
                approvedRecipes);

        model.addAttribute(
                "averageRatings",
                averageRatings);

        model.addAttribute(
                "totalRatings",
                totalRatings);

        model.addAttribute(
                "commentsEnabled",
                commentsEnabled);

        model.addAttribute(
                "ratingsEnabled",
                ratingsEnabled);

        model.addAttribute(
                "favoriteStatus",
                favoriteStatus);

        model.addAttribute(
                "search",
                search);

        return "recipes";
    }


    @GetMapping("/recipe-details")
    public String recipeDetails(
            @RequestParam Long id,
            Model model,
            HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Recipe recipe =
                recipeRepository.findById(id).orElse(null);

        if (recipe == null) {
            return "redirect:/recipes";
        }

        if (!"APPROVED".equalsIgnoreCase(
                recipe.getStatus())) {

            return "redirect:/recipes";
        }


        List<Rating> ratings =
                ratingRepository.findByRecipe(recipe);

        int totalRatings = ratings.size();

        double averageRating = 0;

        if (totalRatings > 0) {

            int sum = 0;

            for (Rating rating : ratings) {
                sum += rating.getValue();
            }

            averageRating =
                    (double) sum / totalRatings;
        }


        List<Comment> comments =
                commentRepository.findByRecipe(recipe);


        Favorite favorite =
                favoriteRepository.findByUserAndRecipe(
                        user,
                        recipe);

        boolean isFavorite =
                favorite != null;


        SystemSetting setting =
                systemSettingRepository
                        .findFirstByOrderByIdAsc();

        boolean commentsEnabled = true;

        boolean ratingsEnabled = true;

        if (setting != null) {

            commentsEnabled =
                    "Comments Enabled".equalsIgnoreCase(
                            setting.getComments());

            ratingsEnabled =
                    "Ratings Enabled".equalsIgnoreCase(
                            setting.getRatings());
        }


        model.addAttribute(
                "recipe",
                recipe);

        model.addAttribute(
                "averageRating",
                averageRating);

        model.addAttribute(
                "totalRatings",
                totalRatings);

        model.addAttribute(
                "comments",
                comments);

        model.addAttribute(
                "commentsEnabled",
                commentsEnabled);

        model.addAttribute(
                "ratingsEnabled",
                ratingsEnabled);

        model.addAttribute(
                "isFavorite",
                isFavorite);

        return "recipe-details";
    }


    @PostMapping("/favorite")
    public String addFavorite(
            @RequestParam Long recipeId,
            HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Recipe recipe =
                recipeRepository.findById(recipeId)
                        .orElse(null);

        if (recipe != null &&
                "APPROVED".equalsIgnoreCase(
                        recipe.getStatus())) {

            Favorite existingFavorite =
                    favoriteRepository.findByUserAndRecipe(
                            user,
                            recipe);

            if (existingFavorite == null) {

                Favorite favorite =
                        new Favorite();

                favorite.setUser(user);

                favorite.setRecipe(recipe);

                favoriteRepository.save(favorite);
            }
        }

        return "redirect:/recipe-details?id=" + recipeId;
    }


    @PostMapping("/unfavorite")
    public String removeFavorite(
            @RequestParam Long recipeId,
            HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Recipe recipe =
                recipeRepository.findById(recipeId)
                        .orElse(null);

        if (recipe != null) {

            Favorite favorite =
                    favoriteRepository.findByUserAndRecipe(
                            user,
                            recipe);

            if (favorite != null) {

                favoriteRepository.delete(favorite);
            }
        }

        return "redirect:/recipe-details?id=" + recipeId;
    }


    @GetMapping("/favorites")
    public String myFavorites(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        List<Favorite> favorites =
                favoriteRepository.findByUser(user);

        model.addAttribute(
                "favorites",
                favorites);

        return "favorites";
    }


    @GetMapping("/my-recipes")
    public String myRecipes(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        List<Recipe> myRecipes =
                recipeRepository.findByUser(user);

        model.addAttribute(
                "recipes",
                myRecipes);

        return "my-recipes";
    }


    @GetMapping("/edit-recipe")
    public String editRecipe(
            @RequestParam Long id,
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Recipe recipe =
                recipeRepository.findById(id)
                        .orElse(null);

        if (recipe == null) {
            return "redirect:/my-recipes";
        }

        if (recipe.getUser() == null ||
                !recipe.getUser().getId()
                        .equals(user.getId())) {

            return "redirect:/my-recipes";
        }

        model.addAttribute(
                "recipe",
                recipe);

        return "edit-recipe";
    }


    @PostMapping("/edit-recipe")
    public String updateRecipe(
            @RequestParam Long id,
            @RequestParam String title,
            @RequestParam String ingredients,
            @RequestParam String instructions,
            @RequestParam(required = false) String imageUrl,
            HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Recipe recipe =
                recipeRepository.findById(id)
                        .orElse(null);

        if (recipe == null) {
            return "redirect:/my-recipes";
        }

        if (recipe.getUser() == null ||
                !recipe.getUser().getId()
                        .equals(user.getId())) {

            return "redirect:/my-recipes";
        }

        recipe.setTitle(title);

        recipe.setIngredients(ingredients);

        recipe.setInstructions(instructions);

        recipe.setImageUrl(imageUrl);


        SystemSetting setting =
                systemSettingRepository
                        .findFirstByOrderByIdAsc();

        if (setting != null &&
                "Automatic Approval".equalsIgnoreCase(
                        setting.getRecipeApproval())) {

            recipe.setStatus("APPROVED");

        } else {

            recipe.setStatus("PENDING");
        }

        recipeRepository.save(recipe);

        return "redirect:/my-recipes";
    }


    @PostMapping("/delete-recipe")
    public String deleteRecipe(
            @RequestParam Long id,
            HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Recipe recipe =
                recipeRepository.findById(id)
                        .orElse(null);

        if (recipe == null) {
            return "redirect:/my-recipes";
        }

        if (recipe.getUser() == null ||
                !recipe.getUser().getId()
                        .equals(user.getId())) {

            return "redirect:/my-recipes";
        }

        recipeRepository.delete(recipe);

        return "redirect:/my-recipes";
    }


    @PostMapping("/add-comment")
    public String addComment(
            @RequestParam Long recipeId,
            @RequestParam String text,
            HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        SystemSetting setting =
                systemSettingRepository
                        .findFirstByOrderByIdAsc();

        if (setting != null &&
                "Comments Disabled".equalsIgnoreCase(
                        setting.getComments())) {

            return "redirect:/recipes";
        }

        Recipe recipe =
                recipeRepository.findById(recipeId)
                        .orElse(null);

        if (recipe != null &&
                "APPROVED".equalsIgnoreCase(
                        recipe.getStatus())) {

            Comment comment =
                    new Comment();

            comment.setText(text);

            comment.setUser(user);

            comment.setRecipe(recipe);

            commentRepository.save(comment);
        }

        return "redirect:/recipes";
    }


    @PostMapping("/add-rating")
    public String addRating(
            @RequestParam Long recipeId,
            @RequestParam int value,
            HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        SystemSetting setting =
                systemSettingRepository
                        .findFirstByOrderByIdAsc();

        if (setting != null &&
                "Ratings Disabled".equalsIgnoreCase(
                        setting.getRatings())) {

            return "redirect:/recipes";
        }

        Recipe recipe =
                recipeRepository.findById(recipeId)
                        .orElse(null);

        if (recipe != null &&
                "APPROVED".equalsIgnoreCase(
                        recipe.getStatus()) &&
                value >= 1 &&
                value <= 5) {

            Rating existingRating =
                    ratingRepository.findByUserAndRecipe(
                            user,
                            recipe);

            if (existingRating != null) {

                existingRating.setValue(value);

                ratingRepository.save(existingRating);

            } else {

                Rating rating =
                        new Rating();

                rating.setValue(value);

                rating.setUser(user);

                rating.setRecipe(recipe);

                ratingRepository.save(rating);
            }
        }

        return "redirect:/recipes";
    }
}