using System;
using System.Collections.Generic;

namespace WebAppTestData.Models;

public partial class Image
{
    public string Id { get; set; } = null!;

    public string ImageUrl { get; set; } = null!;

    public virtual Recipe IdNavigation { get; set; } = null!;
}
