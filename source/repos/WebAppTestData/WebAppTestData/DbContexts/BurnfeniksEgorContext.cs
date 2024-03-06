using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;
using WebAppTestData.Models;

namespace WebAppTestData.DbContexts;

public partial class BurnfeniksEgorContext : DbContext
{
    public BurnfeniksEgorContext()
    {
    }

    public BurnfeniksEgorContext(DbContextOptions<BurnfeniksEgorContext> options)
        : base(options)
    {
    }

    public virtual DbSet<Category> Categories { get; set; }

    public virtual DbSet<Image> Images { get; set; }

    public virtual DbSet<Ingredient> Ingredients { get; set; }

    public virtual DbSet<IngredientAmount> IngredientAmounts { get; set; }

    public virtual DbSet<IngredientPrice> IngredientPrices { get; set; }

    public virtual DbSet<Objectname> Objectnames { get; set; }

    public virtual DbSet<Recipe> Recipes { get; set; }

    public virtual DbSet<Role> Roles { get; set; }

    public virtual DbSet<Shop> Shops { get; set; }

    public virtual DbSet<User> Users { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
#warning To protect potentially sensitive information in your connection string, you should move it out of source code. You can avoid scaffolding the connection string by using the Name= syntax to read it from configuration - see https://go.microsoft.com/fwlink/?linkid=2131148. For more guidance on storing connection strings, see http://go.microsoft.com/fwlink/?LinkId=723263.
        => optionsBuilder.UseNpgsql("Host=postgresql.burnfeniks.myjino.ru;Port=5432;Username=burnfeniks_egor;Password=SqhkpHuu;Database=burnfeniks_egor;Pooling=true;SSL Mode=Prefer;Trust Server Certificate=true;");

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Category>(entity =>
        {
            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.CategoryName).HasMaxLength(30);
        });

        modelBuilder.Entity<Image>(entity =>
        {
            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.ImageUrl).HasColumnName("ImageURL");

            entity.HasOne(d => d.IdNavigation).WithOne(p => p.Image).HasForeignKey<Image>(d => d.Id);
        });

        modelBuilder.Entity<Ingredient>(entity =>
        {
            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.IngredientName).HasMaxLength(30);
        });

        modelBuilder.Entity<IngredientAmount>(entity =>
        {
            entity.HasKey(e => new { e.IngredientId, e.RecipeId });

            entity.HasIndex(e => e.RecipeId, "IX_IngredientAmounts_RecipeID");

            entity.Property(e => e.IngredientId).HasColumnName("IngredientID");
            entity.Property(e => e.RecipeId).HasColumnName("RecipeID");

            entity.HasOne(d => d.Ingredient).WithMany(p => p.IngredientAmounts).HasForeignKey(d => d.IngredientId);

            entity.HasOne(d => d.Recipe).WithMany(p => p.IngredientAmounts).HasForeignKey(d => d.RecipeId);
        });

        modelBuilder.Entity<IngredientPrice>(entity =>
        {
            entity.HasKey(e => new { e.AvailableIngredientsId, e.ShopsWhereAvailableId });

            entity.HasIndex(e => e.IngredientId, "IX_IngredientPrices_IngredientID");

            entity.HasIndex(e => e.ShopId, "IX_IngredientPrices_ShopID");

            entity.HasIndex(e => e.ShopsWhereAvailableId, "IX_IngredientPrices_ShopsWhereAvailableID");

            entity.Property(e => e.AvailableIngredientsId).HasColumnName("AvailableIngredientsID");
            entity.Property(e => e.ShopsWhereAvailableId).HasColumnName("ShopsWhereAvailableID");
            entity.Property(e => e.IngredientId).HasColumnName("IngredientID");
            entity.Property(e => e.ShopId).HasColumnName("ShopID");

            entity.HasOne(d => d.AvailableIngredients).WithMany(p => p.IngredientPriceAvailableIngredients).HasForeignKey(d => d.AvailableIngredientsId);

            entity.HasOne(d => d.Ingredient).WithMany(p => p.IngredientPriceIngredients).HasForeignKey(d => d.IngredientId);

            entity.HasOne(d => d.Shop).WithMany(p => p.IngredientPriceShops).HasForeignKey(d => d.ShopId);

            entity.HasOne(d => d.ShopsWhereAvailable).WithMany(p => p.IngredientPriceShopsWhereAvailables).HasForeignKey(d => d.ShopsWhereAvailableId);
        });

        modelBuilder.Entity<Objectname>(entity =>
        {
            entity
                .HasNoKey()
                .ToTable("objectname");

            entity.Property(e => e.Id)
                .ValueGeneratedOnAdd()
                .HasComment("Идентификатор")
                .HasColumnName("id");
            entity.Property(e => e.Name)
                .HasComment("Название")
                .HasColumnType("character varying");
        });

        modelBuilder.Entity<Recipe>(entity =>
        {
            entity.HasIndex(e => e.CategoryId, "IX_Recipes_CategoryID");

            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.CategoryId).HasColumnName("CategoryID");
            entity.Property(e => e.RecipeDescription).HasMaxLength(6400);
            entity.Property(e => e.RecipeName).HasMaxLength(30);

            entity.HasOne(d => d.Category).WithMany(p => p.Recipes).HasForeignKey(d => d.CategoryId);
        });

        modelBuilder.Entity<Role>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("role_pk");

            entity.ToTable("Role");

            entity.Property(e => e.Id)
                .HasComment("Идентификатор (счетчик)")
                .HasColumnName("id");
            entity.Property(e => e.Name)
                .HasComment("Название роли")
                .HasColumnType("character varying");
        });

        modelBuilder.Entity<Shop>(entity =>
        {
            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.Address).HasMaxLength(100);
            entity.Property(e => e.Name).HasMaxLength(30);
        });

        modelBuilder.Entity<User>(entity =>
        {
            entity
                .HasNoKey()
                .ToTable("User");

            entity.Property(e => e.Id)
                .ValueGeneratedOnAdd()
                .HasComment("Идентификатор (счетчик)")
                .HasColumnName("id");
            entity.Property(e => e.Name)
                .HasComment("Имя пользователя")
                .HasColumnType("character varying")
                .HasColumnName("name");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
