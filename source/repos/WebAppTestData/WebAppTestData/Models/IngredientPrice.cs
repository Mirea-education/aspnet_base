using System;
using System.Collections.Generic;

namespace WebAppTestData.Models;

public partial class IngredientPrice
{
    public string AvailableIngredientsId { get; set; } = null!;

    public string ShopsWhereAvailableId { get; set; } = null!;

    public string? IngredientId { get; set; }

    public string? ShopId { get; set; }

    public float Price { get; set; }

    public virtual Ingredient AvailableIngredients { get; set; } = null!;

    public virtual Ingredient? Ingredient { get; set; }

    public virtual Shop? Shop { get; set; }

    public virtual Shop ShopsWhereAvailable { get; set; } = null!;
}
