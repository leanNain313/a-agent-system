package com.ye.service.impl;

import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.core.util.StrUtil;
import com.ye.Exception.ErrorCode;
import com.ye.Exception.ThrowUtils;
import com.ye.contant.RedisConstant;
import com.ye.service.EmailService;
import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {


    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public boolean sendVerificationEmail(String to, String subject, String code, String type) {
        try {
            // ✅ 准备Thymeleaf模板上下文
            Context context = new Context();
            context.setVariable("code", code);
            context.setVariable("type", type);
            context.setVariable("time",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // ✅ 处理模板生成HTML内容（对应 resources/templates/email/verification-code.html）
            String htmlContent = templateEngine.process("email/verification-code", context);

            // ✅ 创建MIME消息
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // ✅ 发送邮件
            mailSender.send(message);
            logger.info("邮件发送成功：{}", to);
            return true;
        } catch (Exception e) {
            logger.error("邮件发送失败", e);
            return false;
        }
    }

    private String generateRandomCode(int len) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < len; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 发送验证码
     * @param email 邮箱
     * @param type 验证码类型
     * @return 返回验证码
     */
    @Override
    public boolean sendEmailCode(String email, String type) {
        try {
            // 生成6位随机验证码
            String code = generateRandomCode(6);
            log.info("邮箱验证码为：{}", code);
            // 验证码类型对应的邮件主题
            String subject;
            switch (type) {
                case "login":
                    subject = "登录验证码";
                    break;
                case "register":
                    subject = "注册验证码";
                    break;
                case "reset":
                    subject = "重置密码验证码";
                    break;
                case "twoAuth":
                    subject = "校验是本人正在操作";
                    break;
                default:
                    subject = "验证码";
            }

            // 使用邮件服务发送验证码
            boolean b = sendVerificationEmail(email, subject, code, type);
            ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "验证码发送失败");
            // 将验证码存入缓存
            String redisKey = buildKey(email, type);
            redisTemplate.opsForValue().set(redisKey, code,300, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            log.error("发送邮箱验证码失败", e);
            return false;
        }
    }

    /**
     * 查看过期时间
     */
    @Override
    public Long getCodeExpire(String email, String type) {
        String buildKey = buildKey(email, type);
        return redisTemplate.getExpire(buildKey);
    }

    /**
     * 获取验证码
     * @param email 邮箱
     * @param type 验证码类型
     * @return 返回验证码
     */
    @Override
    public String getEmailCode(String email, String type) {
        //        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.CODE_OVERDUE_ERROR);
        return redisTemplate.opsForValue().get(buildKey(email, type));
    }

    /**
     * 移除验证码
     * @param email 邮箱
     * @param type 类型
     */
    @Override
    public void removeEmailCode(String email, String type) {
        redisTemplate.delete(buildKey(email, type));
    }

    private String buildKey(String email, String type) {
        return String.format("%s%s:%s", RedisConstant.REDIS_EMAIL, email, type);
    }

    public void checkImageCode(String email, String imageCode) {
        String key = RedisConstant.LOGIN_CODE + email;
        String code = redisTemplate.opsForValue().get(key);
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.CODE_OVERDUE_ERROR, "图形验证码已过期");
        MathGenerator mathGenerator = new MathGenerator();
        ThrowUtils.throwIf(!mathGenerator.verify(code, imageCode), ErrorCode.PARAMS_ERROR, "图形验证码输入错误");
    }
}
