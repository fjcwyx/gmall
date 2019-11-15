package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class JwtTest {
	private static final String pubKeyPath = "E:\\workspaceIDEA\\project\\tmp\\rsa.pub";

    private static final String priKeyPath = "E:\\workspaceIDEA\\project\\tmp\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        //secret为盐值
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1NzM3NDA1MzN9.O-OxpVoDXlDmNjC2jVN1MDYHt0SqdIGltrW11dm5dgwEYxptM4vjeEbadFMf6KkhgdA1ClcRGl4T2gqtbHZePrOnsQhxZRAk1jApiLQb9fXL6NvexC8c9fjRB2UvDlrLfVEhj70bBQfQjSq5cS1yAHWif7c20IDI-KZY1IeB-2nntEQgTIifZu0X4k-sSf_4Ufe4gBaFSS9e6-H5NtoXJtO0XjPkMeKZhheVtAsr10GLApW6sezMRRFw59Obf39FGkCw1G4ajq5ze1-441dtA6kScAOFBWKrgIFcxc5mlLiTulwI7Q349Ya6xPgIlkBTRNm_45DunpRDT23eQUo-tg";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}