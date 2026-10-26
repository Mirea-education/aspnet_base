CREATE TABLE IF NOT EXISTS course8_stock (
    sku varchar(32) PRIMARY KEY,
    price_cents bigint NOT NULL CHECK (price_cents >= 0),
    available integer NOT NULL CHECK (available >= 0)
);
CREATE TABLE IF NOT EXISTS course8_orders (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_key varchar(64) NOT NULL UNIQUE,
    sku varchar(32) NOT NULL REFERENCES course8_stock(sku),
    quantity integer NOT NULL CHECK (quantity BETWEEN 1 AND 5),
    total_cents bigint NOT NULL CHECK (total_cents >= 0),
    created_at timestamptz NOT NULL
);
INSERT INTO course8_stock(sku, price_cents, available) VALUES ('BOOK', 149990, 5)
ON CONFLICT (sku) DO NOTHING;
