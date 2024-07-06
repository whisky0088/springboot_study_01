package com.example.mapper;

import com.example.entity.auth.Account;
import com.example.entity.user.AccountUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    @Select("select * from demo01.user where username = #{text} or email = #{text}")
    Account findAccountByNameOrEmail(String text);
    @Select("select * from demo01.user where username = #{text} or email = #{text}")
    AccountUser findAccountUserByNameByEmail(String text);
    @Insert("insert into demo01.user (email, username, password) values(#{email}, #{username}, #{password})")
    int createAccount(String username, String password, String email);
    @Update("update demo01.user set password = #{password} where email = #{email}")
    int updatePassword(String email, String password);
}
