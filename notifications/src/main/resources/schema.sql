create schema if not exists notifications;
set search_path to notifications;

create table if not exists processed_notifications (
    operation_id varchar(255) primary key,
    created_at timestamp with time zone not null default now()
);
