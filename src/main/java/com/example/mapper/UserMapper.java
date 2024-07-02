package com.example.mapper;

import com.example.entity.Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from demo01.user where username = #{text} or email = #{text}")
    Account findAccountByNameOrEmail(String text);

    @Insert("insert into demo01.user (email, username, password) values(#{email}, #{username}, #{password})")
    int createAccount(String username, String password, String email);
}
