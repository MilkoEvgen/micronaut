CREATE TABLE departments
(
    id                    BIGSERIAL PRIMARY KEY,
    name                  TEXT NOT NULL,
    head_of_department_id BIGINT references teachers (id) ON DELETE CASCADE,
    UNIQUE (head_of_department_id)
);