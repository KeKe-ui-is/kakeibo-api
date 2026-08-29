package io.github.KeKe_ui_is.kakeibo_api.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DatabaseConnectionMapper {

    @Select("SELECT COUNT(*) FROM users")
    long countUsers();
}
