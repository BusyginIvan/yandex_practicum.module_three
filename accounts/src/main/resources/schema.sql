create schema if not exists accounts;
set search_path to accounts;

create table if not exists accounts (
    login varchar(255) primary key,
    name varchar(255),
    birthdate date,
    balance integer not null
);

create table if not exists balance_operations (
    operation_id varchar(255) primary key,
    login varchar(255) not null,
    type varchar(32) not null check (type in ('DEPOSIT', 'WITHDRAW')),
    amount integer not null,
    status varchar(32) not null check (status in ('PROCESSING', 'SUCCESS', 'INSUFFICIENT_FUNDS'))
);
