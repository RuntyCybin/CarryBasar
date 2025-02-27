-- ::::::::::::::::::::::::::::::::::::::::::::::::::::
-- ::::::::::::::Carry Basar scripts:::::::::::::::::::
-- ::::::::::::::::::::::::::::::::::::::::::::::::::::

CREATE SCHEMA transportsschema;

-- 1.
CREATE TABLE transportsschema.users (
    id serial PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE not null,
    password VARCHAR(100) not null
);

-- 2.
CREATE TABLE transportsschema.roles (
  rol_id serial PRIMARY KEY,
  descripcion varchar(100)
);


-- 3.
CREATE TABLE transportsschema.usuarios_roles (
  usuario_id serial,
  rol_id serial
);


ALTER TABLE transportsschema.usuarios_roles ADD FOREIGN KEY ("usuario_id") REFERENCES transportsschema.users (id);

ALTER TABLE transportsschema.usuarios_roles ADD FOREIGN KEY ("rol_id") REFERENCES transportsschema.roles (rol_id);

CREATE SEQUENCE cybinschema.users_seq;



-- actualiza la secuencia para que comience en 4
ALTER SEQUENCE cybinschema.users_seq RESTART WITH 1;


-- Inserting sample roles
insert into transportsschema.roles (rol_id, descripcion) values
(1, 'USER'),
(2, 'ADMIN'),
(3, 'TRANSPORTER'),
(4, 'CLIENT');

-- table of orders to transport
CREATE TABLE transportsschema.orders (
    id int PRIMARY KEY,
    description VARCHAR(200),
    user_id int references transportsschema.users(id),
    vol int not null,
    order_date timestamp default current_timestamp
);

insert into transportsschema.orders (id, description, user_id, vol, order_date) values (1,'test product', 1, 123, now());
-- ::::::::::::::::::::::::::::::::::::::::::::::::::::
-- ::::::::::::::!Carry Basar scripts::::::::::::::::::
-- ::::::::::::::::::::::::::::::::::::::::::::::::::::


SELECT MAX(id) FROM cybinschema.users;

SELECT users.id, users.name, users.password FROM transportsschema.users WHERE users.name = 'user1';


INSERT INTO transportsschema.usuarios_roles (usuario_id, rol_id) VALUES (1, 1);

SELECT column_name
FROM information_schema.columns
WHERE table_name = 'usuarios_roles';


ALTER TABLE transportsschema.usuarios_roles ADD COLUMN id SERIAL PRIMARY KEY;



















