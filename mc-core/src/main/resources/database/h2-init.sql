CREATE TABLE IF NOT EXISTS cat (
    id int not null IDENTITY,
    name varchar(80),
    create_date TIMESTAMP,
    constraint pk_cat_id primary key (id)
);


CREATE TABLE worker_register2(
    ID INT IDENTITY PRIMARY KEY,
    host VARCHAR(255),
    port INT,
    create_time BIGINT,
    update_time BIGINT,
    constraint idx_hp UNIQUE  (host, port)
);