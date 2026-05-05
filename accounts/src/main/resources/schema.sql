create schema if not exists accounts;
set search_path to accounts;

create table if not exists accounts (
    login varchar(255) primary key,
    name varchar(255),
    birthdate date,
    balance integer not null
);
