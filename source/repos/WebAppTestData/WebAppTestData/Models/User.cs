using System;
using System.Collections.Generic;

namespace WebAppTestData.Models;

public partial class User
{
    /// <summary>
    /// Идентификатор (счетчик)
    /// </summary>
    public int Id { get; set; }

    /// <summary>
    /// Имя пользователя
    /// </summary>
    public string Name { get; set; } = null!;
}
