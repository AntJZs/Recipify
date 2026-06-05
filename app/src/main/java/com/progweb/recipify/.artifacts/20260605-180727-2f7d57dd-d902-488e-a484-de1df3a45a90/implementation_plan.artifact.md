# Expand Add Recipe and Profile Features

Enhance the "Add Recipe" functionality with gallery image upload, ingredients, and instructions. Update the "Profile" fragment to display a grid of recipes published by the user.

## Proposed Changes

### Add Recipe Enhancement

#### [activity_add_recipe.xml](file:///C:/DesarrolloMovil/recipify/Recipify/app/src/main/res/layout/activity_add_recipe.xml)
- Remove `tilTiempo` (optional, keep if needed but user didn't mention it).
- Add `ImageView` for image preview.
- Add `MaterialButton` to "Select Image" from gallery.
- Add `TextInputLayout` for "Ingredients" (multi-line).
- Clarify "Instructions" (Body) label.

#### [AddRecipe.kt](file:///C:/DesarrolloMovil/recipify/Recipify/app/src/main/java/com/progweb/recipify/addRecipe/AddRecipe.kt)
- Use `ActivityResultLauncher` for gallery picking.
- Upload selected image to Firebase Storage and get URL.
- Get current user's `displayName` to include as `authorName` in Firestore.
- Update Firestore `add` call with `ingredients` (List), `imageURL`, `authorName`, and `instructions`.

#### [AddRecipeViewModel.kt](file:///C:/DesarrolloMovil/recipify/Recipify/app/src/main/java/com/progweb/recipify/viewmodel/AddRecipeViewModel.kt)
- Update validation for new required fields (ingredients).

---

### Profile Enhancement

#### [fragment_profile.xml](file:///C:/DesarrolloMovil/recipify/Recipify/app/src/main/res/layout/fragment_profile.xml)
- Add a `RecyclerView` with `GridLayoutManager` (3 columns) below the user info to show "My Recipes".
- Add a header "Mis Recetas".

#### [ProfileFragment.kt](file:///C:/DesarrolloMovil/recipify/Recipify/app/src/main/java/com/progweb/recipify/home/ProfileFragment.kt)
- Fetch recipes from Firestore where `userId == currentUser.uid`.
- Implement a simple adapter for the 3-column grid (image only, similar to Instagram).

---

## Verification Plan

### Manual Verification
1. **Gallery Upload**: Open "Add Recipe", select an image, and verify it uploads to Firebase Storage and shows in the preview.
2. **Save Recipe**: Fill in name, category, ingredients, and instructions. Save and verify Firestore contains `authorName` and the other fields.
3. **Profile Grid**: Go to the Profile tab. Verify that only recipes published by the current user appear in the grid.
4. **Recipe Detail**: Open a published recipe and verify it shows the correct author and content.
