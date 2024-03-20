using System;
using System.Collections.Generic;

namespace TestDevMagistr.DbModelsOdb;

public partial class User
{
    public long Id { get; set; }

    public string Name { get; set; } = null!;

    public string Email { get; set; } = null!;

    public string Password { get; set; } = null!;
}
