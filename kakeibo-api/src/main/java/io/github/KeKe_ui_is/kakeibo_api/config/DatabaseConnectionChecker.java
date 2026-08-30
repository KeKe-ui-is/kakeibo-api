package io.github.KeKe_ui_is.kakeibo_api.config;

import io.github.KeKe_ui_is.kakeibo_api.mapper.DatabaseConnectionMapper;
import io.github.KeKe_ui_is.kakeibo_api.model.Category;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionChecker implements CommandLineRunner {

    private final DatabaseConnectionMapper mapper;

    public DatabaseConnectionChecker(DatabaseConnectionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void run(String... args) {
        long userCount = mapper.countUsers();
        System.out.println(
                "DB接続成功: usersテーブルの件数 = " + userCount
        );
    }
}