create schema if not exists cash;
set search_path to cash;

create table if not exists cash_operations (
    operation_id varchar(255) primary key,
    login varchar(255) not null,
    type varchar(32) not null check (type in ('DEPOSIT', 'WITHDRAW')),
    amount integer not null,
    stage varchar(32) not null check (stage in ('NEW', 'REJECTED_INSUFFICIENT_FUNDS', 'NOTIFICATION_PENDING', 'COMPLETED')),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index if not exists idx_cash_operations_stage_created_at
    on cash_operations (stage, created_at);
