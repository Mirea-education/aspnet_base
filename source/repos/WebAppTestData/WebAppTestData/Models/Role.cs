using System;
using System.Collections.Generic;

namespace WebAppTestData.Models;

public partial class Role
{
    /// <summary>
    /// Идентификатор (счетчик)
    /// </summary>
    public int Id { get; set; }

    /// <summary>
    /// Название роли
    /// </summary>
    public string Name { get; set; } = null!;
}
