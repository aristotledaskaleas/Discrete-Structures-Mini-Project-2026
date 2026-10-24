
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY,
    username TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS products (
    product_id INTEGER PRIMARY KEY,
    product_name TEXT NOT NULL,
    manufacturer TEXT,
    category TEXT
);

CREATE TABLE IF NOT EXISTS user_products (
    user_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,

    PRIMARY KEY (user_id, product_id),

    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    FOREIGN KEY (product_id)
        REFERENCES products(product_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_relationships (
    src_product_id INTEGER NOT NULL,
    dst_product_id INTEGER NOT NULL,

    weight REAL NOT NULL
        CHECK (weight >= 0.0 AND weight <= 1.0),

    PRIMARY KEY (src_product_id, dst_product_id),

    FOREIGN KEY (src_product_id)
        REFERENCES products(product_id)
        ON DELETE CASCADE,

    FOREIGN KEY (dst_product_id)
        REFERENCES products(product_id)
        ON DELETE CASCADE,

    CHECK (src_product_id <> dst_product_id)
);

CREATE INDEX IF NOT EXISTS idx_user_products_product
    ON user_products(product_id);

CREATE INDEX IF NOT EXISTS idx_relationships_target
    ON product_relationships(dst_product_id);