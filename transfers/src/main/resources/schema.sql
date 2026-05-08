create schema if not exists transfers;
set search_path to transfers;

create table if not exists transfer_operations (
    operation_id varchar(255) primary key,
    sender_login varchar(255) not null,
    recipient_login varchar(255) not null,
    amount integer not null,
    withdraw_operation_id varchar(255) not null unique,
    deposit_operation_id varchar(255) not null unique,
    stage varchar(32) not null check (stage in ('NEW', 'WITHDRAW_SUCCEEDED', 'REJECTED_INSUFFICIENT_FUNDS', 'NOTIFICATION_PENDING', 'COMPLETED')),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index if not exists idx_transfer_operations_stage_created_at
    on transfer_operations (stage, created_at);
