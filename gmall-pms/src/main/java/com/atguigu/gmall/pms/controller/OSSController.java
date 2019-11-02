package com.atguigu.gmall.pms.controller;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.atguigu.core.bean.Resp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by FJC on 2019-10-30.
 */
@RestController
@RequestMapping("pms/oss")
public class OSSController {

    @GetMapping("/policy")
    public Resp<Object> Oss() {
        /**
         * 用户登录名称 fan@1297753656065409.onaliyun.com
         * AccessKey ID LTAI4FwSojfRgW7saF97xXEd
         * AccessKeySecret 4HzKz1VvYKQbm8cuIbktO5r7kQPrSs
         */


        String accessId = "LTAI4FwSojfRgW7saF97xXEd"; // 请填写您的AccessKeyId。
        String accessKey = "4HzKz1VvYKQbm8cuIbktO5r7kQPrSs"; // 请填写您的AccessKeySecret。
        String endpoint = "oss-cn-shanghai.aliyuncs.com"; // 请填写您的 endpoint。
        String bucket = "fjc-scw"; // 请填写您的 bucketname 。
        String host = "https://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint
        // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
        Date data = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dir = format.format(data); // 用户上传文件时指定的前缀。

        OSSClient client = new OSSClient(endpoint, accessId, accessKey);
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // respMap.put("expire", formatISO8601Date(expiration));
            return Resp.ok(respMap);

        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
        }
        return null;
    }
}
