CREATE TABLE pet_owners
(
    id                  UUID        NOT NULL PRIMARY KEY,
    created_at          TIMESTAMP   WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP   WITHOUT TIME ZONE NOT NULL,
    pet_owner_detail    JSONB       NOT NULL
);

COMMENT ON TABLE pet_owners IS 'Владельцы питомцев';
COMMENT ON COLUMN pet_owners.id IS 'PK таблицы';
COMMENT ON COLUMN pet_owners.created_at IS 'Дата создания';
COMMENT ON COLUMN pet_owners.updated_at IS 'Дата обновления';
COMMENT ON COLUMN pet_owners.pet_owner_detail IS 'JSON данные владельца питомца';

INSERT INTO pet_owners VALUES('1ab6229a-4e7b-4ac0-a7d0-f40d60ce1e59', now(), now(), '{"name": "PetOwner"}');
