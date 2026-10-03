ALTER TABLE publishers ADD COLUMN name_key VARCHAR(120);

UPDATE publishers SET name_key = LOWER(name);

ALTER TABLE publishers ALTER COLUMN name_key SET NOT NULL;

ALTER TABLE publishers DROP CONSTRAINT uk_publishers_name;

ALTER TABLE publishers ADD CONSTRAINT uk_publishers_name_key UNIQUE (name_key);
