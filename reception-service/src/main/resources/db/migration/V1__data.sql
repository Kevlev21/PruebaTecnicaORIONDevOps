CREATE TABLE IF NOT EXISTS reception_data (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,

    CONSTRAINT uq_recepction_datas_name UNIQUE (name)
);