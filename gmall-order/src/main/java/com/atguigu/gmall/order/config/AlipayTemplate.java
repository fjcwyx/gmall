package com.atguigu.gmall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.order.vo.PayVo;
import lombok.Data;
import org.springframework.stereotype.Component;

//@ConfigurationProperties(prefix = "alipay") 通过配置文件写入信息，这里直接写死
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2016101300678345";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCLclCRTXwY+EdnhLDCIZ3BnBafnoF+9+YcEDncqg0mSjY8gaZIuU4gYsWQSKEl7tqLtt2xSdBh6egEiI7Ca3KAY3WlXRndDAv+AjLGVSSM6wnoJFZyPewzhiEbNLtHPfUUCRVTAjs/oN+QTcxj+jHXc/NeTNy0U1sv5iDsKY/+x0iuIjFDyj0WYLlJmxnTa5y//1FJ1p9Mvde8lzBbZHP7qdUu2NYzvFTjwuip5CO/6EdFe8EKX7T6efIH/EMn8x/pKSFiZjyJQDbYVGBci1PGe8Jc6Q8+uUf43dHJmyLw0sEVnQ0P/y12fUw7ZkVGF06EIi9SnP0BWiuJvrqbmh7rAgMBAAECggEAB0iGOsCgTbGSES8WCF23HqinadjejKIoC6kxTHw5SZmwsYJYDA5ZrUlbZkjU2eVa1tdLRvpC/wo3ix0Sz8bK6TfIqkVtl2UMr0ywGDnUOeQA1XYoabkgQCSuT/x2F6d9yHkc+wPv0kf1xVS66zHjLoyo1nEPSf/ckGjlqVAWbEkqZHF2JZ2wp0b+Aakm7evKC+0F+Ar1OIVjEYC7GhZBDOMrAxEY2npzQB7+3DyAQC64tXUzLuCUu/WUvlgYaocdy9jPEq4szTAhmfWpcMxL/isTC7+UA8A2KqHGTWFVppHoYq9T7EMhAnOqeFn+2GlhKbtDQxJZuXLBrfTtldSKmQKBgQDb+Ox9OR2m12ClSP3LDXgUQAaZSDBb9BwwNS80TGZ6kFR9kpXjZfHzgvbi3UVYeWp8HUEDahsjCVYYXK6s28zM/fIMVWd5RRYjwkMjWWik71uBAmLCgh9IntJ0rqVVh06tkrPz05nFkcBudByysB7qOjHq0otexTqqxG6oPFD0RwKBgQCiSRO0jzYHB3WMlkD5lYJ0s8tnrdE/H0xaDn0avD/tXieBLNGmehRw5C5yAB9RizlLtGOtPd096hYciQ/nntNcC7Po455mRwLmuuF+V72VwGKmtHK5FIkZ/O//Aps7SH4Ys8TH5aNPZPLxq7sC9kfZP89voMe+HBPmun9yaxfGPQKBgGl66dv6QW+5o4xefeSaZtQ/B2sFJLtTSRkMx7/qStHG190HJ3zNa8H7MOwEcrZEMn2lgYFjpfOdDH3OI6ycEY7Szvh/E4khScaH5q3St80vQc+RF05yGxI1pnFxNbLXmTC+QjBPbVNB2uacIw7ESW7DzR/vKgmdxc4RN4bQtXkNAoGAbm7WFvehZ1UfG8Uff/fKp4m1796rVo4A0gOsBGZ4BhodPdz26ZeHk27jYDZPGZlpKugdMEW56SkDVPW8OfytjaR1EBTctHK8ObCQQ/6HB4G8X/7JVsLvrRyfYGQwqD3jeQHT9ceBw0RJwkT/ZLudFuxCR48cfXTz4f2lPoI70m0CgYEApQVMy1/xetinJmB8IsdGZaCB+iLT/hOLgUjPUnIqBb4GWKzHs3fXbF7QIPWFQx5tyDuW4vgYJW6ZUNXAHmiPIKYcdnHEYRzUnFMWjDM7GgDWsDECVACiyXtWXKD0ArPCGiw+a74H4Q/J0yST9M5mb0CDwBfUyeIJoy5iCODwL2c=";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhgdNJ9saskSKPvPa6N7nCrJdI7zn8Lvo77XCpVJT9+bViU46ISvs6gyWu0uIL0CQSgbBYaj/jUfv08GOCrDyINLmrRTpTztWV/0tJviGPlR6+JxyIU9+VQguc0InqBQgucp7JRtpt4XuqNrXyWCdtee8J0CNPUS7xVTDoDzV/n4PgSs0dD6Bu1aWUK3qGvF+dSDFfMM0Bqv6B7oILeO0Y6T2Z1tW8201pdZqDT+v39a5w1HxkbvU6iSEpA2+GmFdLsSfw/RMlQdRsSG+5QSXXqoXwEI9PgdELf3Fml0Vuks4yuVJeEO/+SQ7uQm62cDUgWnSFyqBxjbvDMc+LQ8AfwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url = "http://fvkbifn7p3.52http.net/api/order/pay/success";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = null;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
