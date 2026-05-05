package ru.yandex.practicum.bank.accounts.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.bank.accounts.persistence.entity.AccountEntity;

import java.time.LocalDate;

public interface AccountRepository extends JpaRepository<AccountEntity, String> {

    @Modifying
    @Query(value = """
        insert into accounts(login, name, birthdate, balance)
        values (:login, :name, :birthdate, 0)
        on conflict (login) do update
        set name = excluded.name,
            birthdate = excluded.birthdate
        """, nativeQuery = true)
    int upsertProfile(
        @Param("login") String login,
        @Param("name") String name,
        @Param("birthdate") LocalDate birthdate
    );
}
