CREATE TABLE IF NOT EXISTS course_stock (
    sku varchar(32) PRIMARY KEY,
    available integer NOT NULL CHECK (available >= 0)
);
CREATE TABLE IF NOT EXISTS course_orders (
    request_id uuid PRIMARY KEY,
    sku varchar(32) NOT NULL REFERENCES course_stock(sku),
    quantity integer NOT NULL CHECK (quantity > 0)
);
INSERT INTO course_stock(sku, available) VALUES ('BOOK', 5)
ON CONFLICT (sku) DO NOTHING;
