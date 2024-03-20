using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using TestDevMagistr.DbModelsOdb;

namespace TestDevMagistr.Controllers
{
    public class AccountController : Controller
    {
        private readonly BurnfeniksTodoContext _context;
        public AccountController(BurnfeniksTodoContext context)
        {
            _context = context;
        }

        #region View базовых страниц
        public IActionResult Index() => View();
        public IActionResult Login() => View();
        public IActionResult Profile() => View();
        #endregion


        [HttpPost]
        public async Task<IActionResult> Login(User userModel)
        {
            ModelState.Remove("Id");
            ModelState.Remove("Name");

            if (ModelState.IsValid)
            {
                var userList = await _context.Users.FirstOrDefaultAsync(
                    x => x.Email == userModel.Email && x.Password == userModel.Password);
                if (userList != null)
                {
                    return RedirectToAction("Profile");
                }
            }
            return View(userModel);
        }
    }
}
