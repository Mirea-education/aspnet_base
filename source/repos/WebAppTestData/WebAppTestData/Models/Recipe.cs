using System;
using System.Collections.Generic;

namespace WebAppTestData.Models;

public partial class Recipe
{
    public string Id { get; set; } = null!;

    public string RecipeName { get; set; } = null!;

    public DateTime CreationTime { get; set; }

    public string CategoryId { get; set; } = null!;

    public string RecipeDescription { get; set; } = null!;

    public string Rating { get; set; } = null!;

    public float TimeToCook { get; set; }

    public int TimesVisited { get; set; }

    public int TimesLiked { get; set; }

    public int TimesDisliked { get; set; }

    public virtual Category Category { get; set; } = null!;

    public virtual Image? Image { get; set; }

    public virtual ICollection<IngredientAmount> IngredientAmounts { get; } = new List<IngredientAmount>();
}
