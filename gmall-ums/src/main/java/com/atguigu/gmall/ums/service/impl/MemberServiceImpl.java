package com.atguigu.gmall.ums.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.service.MemberService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberDao memberDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<MemberEntity> wrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                wrapper.eq("username", data);
                break;
            case 2:
                wrapper.eq("mobile", data);
                break;
            case 3:
                wrapper.eq("email", data);
                break;
            default:
                return null;
        }
        return this.memberDao.selectCount(wrapper) == 0;
    }

    @Override
    public void register(MemberEntity memberEntity, String code) {
// 校验短信验证码
        // String cacheCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + memberEntity.getMobile());
        // if (!StringUtils.equals(code, cacheCode)) {
        //     return false;
        // }

        // 生成盐
        String salt = StringUtils.replace(UUID.randomUUID().toString(), "-", "");
        memberEntity.setSalt(salt);

        // 对密码加密
        memberEntity.setPassword(DigestUtils.md5Hex(salt + DigestUtils.md5Hex(memberEntity.getPassword())));

        // 设置创建时间等
        memberEntity.setCreateTime(new Date());

        // 添加到数据库
        boolean b = this.save(memberEntity);

        // if(b){
        // 注册成功，删除redis中的记录
        // this.redisTemplate.delete(KEY_PREFIX + memberEntity.getMobile());
        // }
    }

    @Override
    public MemberEntity queryUser(String username, String password) {
        // 查询
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("username", username));
        // 校验用户名
        if (memberEntity == null) {
            return null;
        }
        // 校验密码
        if (!memberEntity.getPassword().equals(DigestUtils.md5Hex(memberEntity.getSalt() + DigestUtils.md5Hex(password)))) {
            return null;
        }
        // 用户名密码都正确
        return memberEntity;
    }

}