package com.example.service.impl;

import com.example.entity.Account;
import com.example.mapper.UserMapper;
import com.example.service.AuthorizeService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthorizeServiceImpl implements AuthorizeService {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizeServiceImpl.class);

    @Value("${spring.mail.username}")
    String from;
    @Resource
    UserMapper mapper;
    @Resource
    MailSender mailSender;
    @Resource
    StringRedisTemplate tpl;
    BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username==null) throw new UsernameNotFoundException("用户名不能为空");
        Account account=mapper.findAccountByNameOrEmail(username);
        if (account==null) throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(account.getUsername())
                .password(account.getPassword())
                .roles("user")
                .build();
    }

    @Override
    public String sendValidateEmail(String email,String sessionId){
        String key = "email:"+sessionId+":"+email;
        if (Boolean.TRUE.equals(tpl.hasKey(key))) {
            Long expire = tpl.getExpire(key, TimeUnit.SECONDS);
            if (expire != null && expire > 120) {
                return "请求频繁，请稍后再试"; // 如果验证码还存在且未过期，则不发送新的验证码
            }
        }
        if(mapper.findAccountByNameOrEmail(email)!=null){
            return "此邮箱已被其他用户注册"; // 如果邮箱已经存在，则不发送新的验证码
        }
        Random random = new Random();
        int code = random.nextInt(899999)+100000;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("验证邮箱");
        message.setText("您的验证码为"+code);
        try{
            mailSender.send(message);
            tpl.opsForValue().set(key,String.valueOf(code),3, TimeUnit.MINUTES);
            return null;
        } catch (Exception e){
            logger.error("发送邮件失败",e);
            return "发送邮件失败";
        }
    }

    @Override
    public String validateAndRegister(String username, String password, String email, String code,String sessionId) {
        String key = "email:"+sessionId+":"+email;
        if(Boolean.TRUE.equals(tpl.hasKey(key))){
            String s = tpl.opsForValue().get(key);
            if (s==null) return "验证码已过期";
            if(s.equals(code)){
                password=encoder.encode(password);
                if(mapper.createAccount(username,password,email)>0){
                    return null;
                }else{
                    return "内部错误，请联系管理员";
                }
            }else{
                return "验证码错误";
            }
        }else{
            return "请先请求一封验证码邮件";
        }
    }
}
