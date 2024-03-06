using System;
using System.Collections.Generic;

namespace WebAppTestData.Models;

public partial class Ingredient
{
    public string Id { get; set; } = null!;

    public string IngredientName { get; set; } = null!;

    public virtual ICollection<IngredientAmount> IngredientAmounts { get; } = new List<IngredientAmount>();

    public virtual ICollection<IngredientPrice> IngredientPriceAvailableIngredients { get; } = new List<IngredientPrice>();

    public virtual ICollection<IngredientPrice> IngredientPriceIngredients { get; } = new List<IngredientPrice>();
}
