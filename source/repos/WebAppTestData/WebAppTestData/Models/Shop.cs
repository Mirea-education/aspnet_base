using System;
using System.Collections.Generic;

namespace WebAppTestData.Models;

public partial class Shop
{
    public string Id { get; set; } = null!;

    public string Name { get; set; } = null!;

    public string Address { get; set; } = null!;

    public virtual ICollection<IngredientPrice> IngredientPriceShops { get; } = new List<IngredientPrice>();

    public virtual ICollection<IngredientPrice> IngredientPriceShopsWhereAvailables { get; } = new List<IngredientPrice>();
}
