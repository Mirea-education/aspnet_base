using System;
using System.Collections.Generic;

namespace WebAppTestData.Models;

public partial class Objectname
{
    /// <summary>
    /// Идентификатор
    /// </summary>
    public int Id { get; set; }

    /// <summary>
    /// Название
    /// </summary>
    public string Name { get; set; } = null!;
}
