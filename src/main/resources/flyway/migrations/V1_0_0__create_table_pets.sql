CREATE TABLE pets
(
    id              UUID        NOT NULL PRIMARY KEY,
    created_at      TIMESTAMP   WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP   WITHOUT TIME ZONE NOT NULL,
    pet_detail      JSONB       NOT NULL
);

CREATE INDEX pets_client_id_idx ON pets USING HASH((pet_detail ->> 'clientId'));
COMMENT ON INDEX pets_client_id_idx IS 'Поиск питомцев по клиенту';

COMMENT ON TABLE pets IS 'Питомцы';
COMMENT ON COLUMN pets.id IS 'PK таблицы';
COMMENT ON COLUMN pets.created_at IS 'Дата создания';
COMMENT ON COLUMN pets.updated_at IS 'Дата обновления';
COMMENT ON COLUMN pets.pet_detail IS 'JSON данные питомца';
