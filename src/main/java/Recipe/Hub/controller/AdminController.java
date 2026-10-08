package Recipe.Hub.controller;

import Recipe.Hub.model.User;
import Recipe.Hub.model.Recipe;
import Recipe.Hub.model.Comment;
import Recipe.Hub.model.Rating;
import Recipe.Hub.model.SystemSetting;
import Recipe.Hub.model.Favorite;

import Recipe.Hub.repository.UserRepository;
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

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final FavoriteRepository favoriteRepository;


    public AdminController(
            UserRepository userRepository,
            RecipeRepository recipeRepository,
            CommentRepository commentRepository,
            RatingRepository ratingRepository,
            SystemSettingRepository systemSettingRepository,
            FavoriteRepository favoriteRepository) {

        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.commentRepository = commentRepository;
        this.ratingRepository = ratingRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.favoriteRepository = favoriteRepository;
    }


    // =========================
    // ADMIN DASHBOARD
    // =========================

    @GetMapping("/admin/dashboard")
    public String adminDashboard(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);

        return "admin-dashboard";
    }


    // =========================
    // MANAGE USERS
    // =========================

    @GetMapping("/admin/users")
    public String manageUsers(
            HttpSession session,
            Model model) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return "redirect:/dashboard";
        }

        List<User> users =
                userRepository.findAll();

        model.addAttribute("users", users);

        return "admin-users";
    }


    // =========================
    // DELETE USER
    // =========================

    @PostMapping("/admin/users/delete")
    public String deleteUser(
            @RequestParam Long id,
            HttpSession session) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return "redirect:/dashboard";
        }

        User user =
                userRepository.findById(id)
                        .orElse(null);

        if (user != null) {

            // Delete user's recipes first
            List<Recipe> recipes =
                    recipeRepository.findByUser(user);

            for (Recipe recipe : recipes) {

                List<Comment> comments =
                        commentRepository.findByRecipe(recipe);

                if (comments != null &&
                        !comments.isEmpty()) {

                    commentRepository.deleteAll(comments);
                }


                List<Rating> ratings =
                        ratingRepository.findByRecipe(recipe);

                if (ratings != null &&
                        !ratings.isEmpty()) {

                    ratingRepository.deleteAll(ratings);
                }


                List<Favorite> favorites =
                        favoriteRepository.findAll();

                for (Favorite favorite : favorites) {

                    if (favorite.getRecipe() != null &&
                            favorite.getRecipe().getId()
                                    .equals(recipe.getId())) {

                        favoriteRepository.delete(favorite);
                    }
                }

                recipeRepository.delete(recipe);
            }


            // Delete user's own favorites
            List<Favorite> userFavorites =
                    favoriteRepository.findByUser(user);

            if (userFavorites != null &&
                    !userFavorites.isEmpty()) {

                favoriteRepository.deleteAll(userFavorites);
            }


            userRepository.delete(user);
        }

        return "redirect:/admin/users";
    }


    // =========================
    // MANAGE RECIPES
    // =========================

    @GetMapping("/admin/recipes")
    public String manageRecipes(
            HttpSession session,
            Model model) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return "redirect:/dashboard";
        }


        List<Recipe> recipes =
                recipeRepository.findAll();


        Map<Long, List<Rating>> recipeRatings =
                new HashMap<>();

        Map<Long, List<Comment>> recipeComments =
                new HashMap<>();

        Map<Long, Double> averageRatings =
                new HashMap<>();

        Map<Long, Integer> totalRatings =
                new HashMap<>();


        for (Recipe recipe : recipes) {

            // =========================
            // RATINGS
            // =========================

            List<Rating> ratings =
                    ratingRepository.findByRecipe(recipe);

            recipeRatings.put(
                    recipe.getId(),
                    ratings
            );


            int total =
                    ratings.size();

            double average = 0;


            if (total > 0) {

                int sum = 0;

                for (Rating rating : ratings) {

                    sum += rating.getValue();
                }

                average =
                        (double) sum / total;
            }


            averageRatings.put(
                    recipe.getId(),
                    average
            );


            totalRatings.put(
                    recipe.getId(),
                    total
            );


            // =========================
            // COMMENTS
            // =========================

            List<Comment> comments =
                    commentRepository.findByRecipe(recipe);

            recipeComments.put(
                    recipe.getId(),
                    comments
            );
        }


        model.addAttribute(
                "recipes",
                recipes
        );

        model.addAttribute(
                "recipeRatings",
                recipeRatings
        );

        model.addAttribute(
                "recipeComments",
                recipeComments
        );

        model.addAttribute(
                "averageRatings",
                averageRatings
        );

        model.addAttribute(
                "totalRatings",
                totalRatings
        );


        return "admin-recipes";
    }


    // =========================
    // APPROVE RECIPE
    // =========================

    @PostMapping("/admin/recipes/approve")
    public String approveRecipe(
            @RequestParam Long id,
            HttpSession session) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return "redirect:/dashboard";
        }


        Recipe recipe =
                recipeRepository.findById(id)
                        .orElse(null);


        if (recipe != null) {

            recipe.setStatus("APPROVED");

            recipeRepository.save(recipe);
        }


        return "redirect:/admin/recipes";
    }


    // =========================
    // REJECT RECIPE
    // =========================

    @PostMapping("/admin/recipes/reject")
    public String rejectRecipe(
            @RequestParam Long id,
            HttpSession session) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return "redirect:/dashboard";
        }


        Recipe recipe =
                recipeRepository.findById(id)
                        .orElse(null);


        if (recipe != null) {

            recipe.setStatus("REJECTED");

            recipeRepository.save(recipe);
        }


        return "redirect:/admin/recipes";
    }


    // =========================
    // DELETE RECIPE
    // =========================

    @PostMapping("/admin/recipes/delete")
    public String deleteRecipe(
            @RequestParam Long id,
            HttpSession session) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return "redirect:/dashboard";
        }


        Recipe recipe =
                recipeRepository.findById(id)
                        .orElse(null);


        if (recipe != null) {


            // =========================
            // DELETE COMMENTS
            // =========================

            List<Comment> comments =
                    commentRepository.findByRecipe(recipe);

            if (comments != null &&
                    !comments.isEmpty()) {

                commentRepository.deleteAll(comments);
            }


            // =========================
            // DELETE RATINGS
            // =========================

            List<Rating> ratings =
                    ratingRepository.findByRecipe(recipe);

            if (ratings != null &&
                    !ratings.isEmpty()) {

                ratingRepository.deleteAll(ratings);
            }


            // =========================
            // DELETE FAVORITES
            // =========================

            List<Favorite> favorites =
                    favoriteRepository.findAll();

            for (Favorite favorite : favorites) {

                if (favorite.getRecipe() != null &&
                        favorite.getRecipe().getId()
                                .equals(recipe.getId())) {

                    favoriteRepository.delete(favorite);
                }
            }


            // =========================
            // DELETE RECIPE
            // =========================

            recipeRepository.delete(recipe);
        }


        return "redirect:/admin/recipes";
    }


    // =========================
    // DELETE COMMENT
    // =========================

    @PostMapping("/admin/comments/delete")
    public String deleteComment(
            @RequestParam Long id,
            HttpSession session) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return "redirect:/dashboard";
        }


        commentRepository.deleteById(id);


        return "redirect:/admin/recipes";
    }


    // =========================
    // DELETE RATING
    // =========================

    @PostMapping("/admin/ratings/delete")
    public String deleteRating(
            @RequestParam Long id,
            HttpSession session) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return "redirect:/dashboard";
        }


        ratingRepository.deleteById(id);


        return "redirect:/admin/recipes";
    }


    // =========================
    // SYSTEM SETTINGS - OPEN
    // =========================

    @GetMapping("/admin/settings")
    public String adminSettings(
            HttpSession session,
            Model model) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return "redirect:/dashboard";
        }


        SystemSetting setting;


        if (systemSettingRepository.count() == 0) {

            setting =
                    new SystemSetting();

            setting.setRecipeApproval(
                    "Admin Approval Required"
            );

            setting.setComments(
                    "Comments Enabled"
            );

            setting.setRatings(
                    "Ratings Enabled"
            );


            setting =
                    systemSettingRepository
                            .save(setting);

        } else {

            setting =
                    systemSettingRepository
                            .findAll()
                            .get(0);
        }


        model.addAttribute(
                "setting",
                setting
        );


        return "admin-settings";
    }


    // =========================
    // SYSTEM SETTINGS - SAVE
    // =========================

    @PostMapping("/admin/settings")
    public String saveSettings(
            @RequestParam String recipeApproval,
            @RequestParam String comments,
            @RequestParam String ratings,
            HttpSession session) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return "redirect:/dashboard";
        }


        SystemSetting setting;


        if (systemSettingRepository.count() == 0) {

            setting =
                    new SystemSetting();

        } else {

            setting =
                    systemSettingRepository
                            .findAll()
                            .get(0);
        }


        setting.setRecipeApproval(
                recipeApproval
        );

        setting.setComments(
                comments
        );

        setting.setRatings(
                ratings
        );


        systemSettingRepository.save(
                setting
        );


        return "redirect:/admin/settings";
    }

}