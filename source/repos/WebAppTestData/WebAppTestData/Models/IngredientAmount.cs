using System;
using System.Collections.Generic;

namespace WebAppTestData.Models;

public partial class IngredientAmount
{
    public string IngredientId { get; set; } = null!;

    public string RecipeId { get; set; } = null!;

    public string Amount { get; set; } = null!;

    public virtual Recipe Ingredient { get; set; } = null!;

    public virtual Ingredient Recipe { get; set; } = null!;
}
