using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using WebMVCVeiwSite.DbContextOdb;
using WebMVCVeiwSite.DbModels;

namespace WebMVCVeiwSite.Controllers
{
    public class AccountController : Controller
    {
        private readonly BurnfeniksTodoContext _context;
        // Отображение формы логина
        public AccountController(BurnfeniksTodoContext context)
        {
            _context = context;
        }
        public IActionResult Login()
        {
            return View();
        }

        // POST: Обработка формы логина
        [HttpPost]
        public async Task<IActionResult> Login(User userModel)
        {
            ModelState.Remove("Id");
            ModelState.Remove("Name");

            if (ModelState.IsValid && _context != null)
            {
                // Проверка учетных данных пользователя
                var user = await _context.Users.ToListAsync();
                if (user != null)
                {
                    // Установка сообщения об успехе
                    //TempData["Success"] = "Вы успешно авторизовались!";
                    return RedirectToAction("Profile"); // или на другую страницу
                }
                ModelState.AddModelError("", "Неверные логин или пароль");
            }
            return View(userModel);
        }

        // Отображение формы регистрации
        public IActionResult Register()
        {
            return View();
        }

        // POST: Обработка формы регистрации
        [HttpPost]
        public IActionResult Register(User userModel)
        {
            ModelState.Remove("Id");
            ModelState.Remove("Name");
            if (ModelState.IsValid && _context != null)
            {
                // Проверка учетных данных пользователя

                User userData = new User()
                {
                    Name = "Test",
                    email = userModel.email,
                    password = userModel.password
                };

                _context.Users.Add(userData);
                _context.SaveChanges();

                if (userData != null)
                {
                    // Установка сообщения об успехе
                    return RedirectToAction("Login"); // или на другую страницу
                }
                ModelState.AddModelError("", "Неверные логин или пароль");
            }
            return RedirectToAction("Index", "Home");
        }

        public IActionResult Profile() { return View(); }
    }
}
