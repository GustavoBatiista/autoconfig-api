-- Technical audit columns (Spring Data JPA Auditing).
-- Apply when migrating an existing database without Hibernate ddl-auto.
-- Default profile uses spring.jpa.hibernate.ddl-auto=update; enable Flyway if you prefer versioned DDL.

ALTER TABLE users ADD COLUMN created_at DATETIME(6) NULL;
ALTER TABLE users ADD COLUMN updated_at DATETIME(6) NULL;
ALTER TABLE users ADD COLUMN created_by VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN updated_by VARCHAR(255) NULL;

ALTER TABLE client ADD COLUMN created_at DATETIME(6) NULL;
ALTER TABLE client ADD COLUMN updated_at DATETIME(6) NULL;
ALTER TABLE client ADD COLUMN created_by VARCHAR(255) NULL;
ALTER TABLE client ADD COLUMN updated_by VARCHAR(255) NULL;

ALTER TABLE car ADD COLUMN created_at DATETIME(6) NULL;
ALTER TABLE car ADD COLUMN updated_at DATETIME(6) NULL;
ALTER TABLE car ADD COLUMN created_by VARCHAR(255) NULL;
ALTER TABLE car ADD COLUMN updated_by VARCHAR(255) NULL;

ALTER TABLE accessory ADD COLUMN created_at DATETIME(6) NULL;
ALTER TABLE accessory ADD COLUMN updated_at DATETIME(6) NULL;
ALTER TABLE accessory ADD COLUMN created_by VARCHAR(255) NULL;
ALTER TABLE accessory ADD COLUMN updated_by VARCHAR(255) NULL;

ALTER TABLE orders ADD COLUMN created_at DATETIME(6) NULL;
ALTER TABLE orders ADD COLUMN updated_at DATETIME(6) NULL;
ALTER TABLE orders ADD COLUMN created_by VARCHAR(255) NULL;
ALTER TABLE orders ADD COLUMN updated_by VARCHAR(255) NULL;

ALTER TABLE vehicle_entry ADD COLUMN created_at DATETIME(6) NULL;
ALTER TABLE vehicle_entry ADD COLUMN updated_at DATETIME(6) NULL;
ALTER TABLE vehicle_entry ADD COLUMN created_by VARCHAR(255) NULL;
ALTER TABLE vehicle_entry ADD COLUMN updated_by VARCHAR(255) NULL;
