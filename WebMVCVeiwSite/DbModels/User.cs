using System;
using System.Collections.Generic;

namespace WebMVCVeiwSite.DbModels;

public partial class User
{
    public long Id { get; set; }

    public string Name { get; set; } = null!;

    public string email { get; set; } = null!;

    public string password { get; set; } = null!;
}
