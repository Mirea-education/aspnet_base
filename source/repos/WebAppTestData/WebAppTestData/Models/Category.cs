using System;
using System.Collections.Generic;

namespace WebAppTestData.Models;

public partial class Category
{
    public string Id { get; set; } = null!;

    public string CategoryName { get; set; } = null!;

    public virtual ICollection<Recipe> Recipes { get; } = new List<Recipe>();
}
