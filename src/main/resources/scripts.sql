-- ::::::::::::::::::::::::::::::::::::::::::::::::::::
-- ::::::::::::::Carry Basar scripts:::::::::::::::::::
-- ::::::::::::::::::::::::::::::::::::::::::::::::::::

CREATE SCHEMA transportsschema;

-- 1.
CREATE TABLE transportsschema.users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) not null,
    email VARCHAR(100) UNIQUE not null,
    password VARCHAR(100) not null
);

-- 2.
CREATE TABLE transportsschema.roles (
  id SERIAL PRIMARY KEY,
  descripcion varchar(100)
);

-- 3.
CREATE TABLE transportsschema.usuarios_roles (
  id SERIAL PRIMARY KEY,
  usuario SERIAL,
  rol SERIAL
);

-- 4.
CREATE TABLE transportsschema.orders (
    id SERIAL PRIMARY KEY,
    description VARCHAR(200),
    user_id int references transportsschema.users(id),
    vol int not null,
    order_date timestamp default current_timestamp
);


ALTER TABLE transportsschema.usuarios_roles ADD FOREIGN KEY ("usuario") REFERENCES transportsschema.users (id);

ALTER TABLE transportsschema.usuarios_roles ADD FOREIGN KEY ("rol") REFERENCES transportsschema.roles (id);

CREATE SEQUENCE transportsschema.users_seq;

-- actualiza la secuencia para que comience en 4
ALTER SEQUENCE transportsschema.users_seq RESTART WITH 1;


-- Inserts
insert into transportsschema.roles (id, descripcion) values
(1, 'USER'),
(2, 'ADMIN'),
(3, 'TRANSPORTER'),
(4, 'CARRY');

-- Inserting data into the orders table
INSERT INTO transportsschema.orders (id, description, user_id, vol, order_date) VALUES
(1, 'Laptop', 1, 23, NOW()),
(2, 'Smartphone', 2, 56, NOW()),
(3, 'Tablet', 1, 45, NOW()),
(4, 'Headphones', 3, 34, NOW()),
(5, 'Monitor', 1, 22, NOW());

INSERT INTO transportsschema.usuarios_roles (usuario_id, rol_id) VALUES (1, 1);