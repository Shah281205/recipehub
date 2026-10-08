# 🍳 Recipe Hub

## Online Recipe Sharing Platform

Recipe Hub is a web-based recipe sharing platform developed using Java and Spring Boot.

Users can create and share recipes, discover recipes, give ratings, write comments, and save their favorite recipes.

Administrators can manage users, recipes, comments, ratings, and system settings.

---

## 🎯 Project Objectives

- Allow users to register and login.
- Allow users to share their own recipes.
- Allow users to discover approved recipes.
- Allow users to search recipes.
- Allow users to rate recipes.
- Allow users to comment on recipes.
- Allow users to save favorite recipes.
- Allow users to edit and delete their recipes.
- Allow administrators to manage users.
- Allow administrators to approve or reject recipes.
- Allow administrators to delete recipes.
- Allow administrators to manage comments and ratings.
- Allow administrators to control system settings.

---

## 🛠️ Technologies Used

### Frontend
- HTML
- CSS
- Thymeleaf

### Backend
- Java
- Spring Boot
- Spring MVC
- Spring Data JPA

### Database
- MySQL

### Build Tool
- Maven

### Development Environment
- Visual Studio Code
- MySQL Workbench

---

## 👤 User Features

### 1. Registration
Users can create a new Recipe Hub account.

### 2. Login
Registered users can securely access their dashboard.

### 3. Share Recipe
Users can add:

- Recipe title
- Ingredients
- Instructions
- Recipe image URL

### 4. Discover Recipes
Users can view approved recipes shared by other users.

### 5. Search Recipes
Users can search recipes by title or ingredients.

### 6. Recipe Details
Users can view complete recipe information.

### 7. Rating
Users can give a rating from 1 to 5 stars.

### 8. Comments
Users can write comments on recipes.

### 9. Favorites
Users can bookmark recipes and view them later.

### 10. My Recipes
Users can view, edit, and delete their own recipes.

### 11. Profile
Users can manage their profile information.

---

## 👑 Admin Features

### Admin Dashboard
Administrator can access the complete management dashboard.

### User Management
Admin can:

- View all users
- View user name
- View email
- View role
- Delete users

### Recipe Management
Admin can:

- View all recipes
- View recipe details
- Approve recipes
- Reject recipes
- Delete recipes
- View ratings
- View comments
- Delete comments
- Delete ratings

### System Settings

Admin can control:

- Recipe approval mode
- Comments
- Ratings

---

## 🗄️ Database

The project uses a MySQL database named:

`recipe_hub`

### Main Tables

- `users`
- `recipes`
- `favorites`
- `comments`
- `ratings`
- `system_settings`

---

## 🔄 Project Workflow

```text
User
  |
  v
Register
  |
  v
Login
  |
  v
User Dashboard
  |
  +----> Share Recipe
  |
  +----> Discover Recipes
  |
  +----> Search Recipes
  |
  +----> Recipe Details
  |          |
  |          +----> Rating
  |          |
  |          +----> Comment
  |          |
  |          +----> Favorite
  |
  +----> My Recipes
  |
  +----> My Favorites
  |
  +----> Profile


Admin
  |
  v
Admin Login
  |
  v
Admin Dashboard
  |
  +----> Manage Users
  |
  +----> Manage Recipes
  |          |
  |          +----> Approve
  |          |
  |          +----> Reject
  |          |
  |          +----> Delete
  |
  +----> Manage Comments/Ratings
  |
  +----> System Settings