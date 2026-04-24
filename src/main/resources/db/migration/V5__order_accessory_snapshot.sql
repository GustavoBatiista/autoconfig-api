RENAME TABLE order_accessory TO order_accessory_old;

CREATE TABLE order_accessory (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    accessory_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    created_by VARCHAR(255) NULL,
    updated_by VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_accessory_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_accessory_accessory FOREIGN KEY (accessory_id) REFERENCES accessory (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO order_accessory (order_id, accessory_id, name, price, created_at, updated_at, created_by, updated_by)
SELECT
    oa.order_id,
    oa.accessory_id,
    a.name,
    a.price,
    a.created_at,
    a.updated_at,
    a.created_by,
    a.updated_by
FROM order_accessory_old oa
INNER JOIN accessory a ON a.id = oa.accessory_id;

DROP TABLE order_accessory_old;
