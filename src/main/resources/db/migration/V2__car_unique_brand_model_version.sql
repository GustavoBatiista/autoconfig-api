-- Multiple cars may share the same brand; uniqueness matches CarServiceImpl (brand + model + version, case-insensitive in app; DB uses table collation).
ALTER TABLE car DROP INDEX uk_car_brand;

ALTER TABLE car
    ADD UNIQUE KEY uk_car_brand_model_version (brand, model, version);
