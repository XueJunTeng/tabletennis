package com.example.tabletennis.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import javax.crypto.SecretKey;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")  // 绑定 application.yml 中的 jwt 配置
public class JwtConfig {
    private String secret;
    private long expiration;
    
    // 生成JWT签名密钥
    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 获取签名算法（HS512）
    public SignatureAlgorithm getSignatureAlgorithm() {
        return SignatureAlgorithm.HS512;
    }
}