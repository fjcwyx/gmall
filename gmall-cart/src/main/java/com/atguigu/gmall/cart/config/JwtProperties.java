package com.atguigu.gmall.cart.config;

import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

    private String publicKeyPath;

    private String cookieName;

    private PublicKey publicKey;

    private String userKeyName;

    private Integer expire;

    @PostConstruct
    public void init(){

        try {
            // 3. 读取密钥
            publicKey = RsaUtils.getPublicKey(publicKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥和私钥失败！！！");
            e.printStackTrace();
        }
    }
}