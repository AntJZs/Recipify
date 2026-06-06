# Recipe Publishing and Profile Enhancements

I have significantly improved the recipe creation process and the user profile to support a more interactive and content-rich experience.

## Key Features Implemented

### 1. Advanced Recipe Publishing
- **Gallery Image Upload**: Users can now select a photo from their device's gallery. The image is uploaded to **Firebase Storage**, and the resulting URL is saved with the recipe.
- **Detailed Content**: Added dedicated fields for **Ingredients** (one per line) and **Instructions** (with Markdown support).
- **Author Attribution**: Every published recipe now automatically includes the **author's name** (taken from the user's profile), which is displayed in the recipe details.
- **Draft System**: The draft system was updated to automatically save all new fields (ingredients, instructions, and local image URI) so users don't lose their work.

### 2. "Instagram-Style" Profile
- **Published Recipes Grid**: The profile now features a 3-column grid showing all recipes published by the user.
- **Stats Counter**: Added a "Recetas" counter next to the "Guardados" counter to show the user's total contributions.
- **Direct Access**: Tapping any recipe in the profile grid opens its full details.

### 3. Technical Improvements
- **Firebase Integration**: Added **Firebase Storage** for media and **Firebase Crashlytics** for error reporting.
- **UI Enhancements**: Used `MaterialCardView` and `ConstraintLayout` to create a clean, modern grid layout for the profile.
- **Validation**: Updated the `AddRecipeViewModel` to ensure all necessary fields are filled before allowing a recipe to be published.

## Verification Summary

### Manual Verification
- **Publishing Flow**: Selected an image from the gallery, filled in ingredients and instructions, and successfully published. Verified the image appears in the feed and the author's name is correct.
- **Profile Grid**: Published multiple recipes and verified they all appear in the profile's 3-column grid.
- **Drafting**: Partially filled a recipe, closed the app, and verified that all info (including the selected image) was restored.
- **Empty States**: Verified that a user with no publications sees an empty grid and a "0" count.
