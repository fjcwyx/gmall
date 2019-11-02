package com.atguigu.gmall.pms.controller;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.AttrGroupVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


/**
 * 属性分组
 *
 * @author fjc
 * @email 1134118878@qq.com
 * @date 2019-10-28 23:08:48
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    @ApiOperation("根据分类id查询组和组下的所有信息")
    @GetMapping("/withattrs/cat/{catId}")
    public Resp<List<AttrGroupVO>> queryAttrByCId(@PathVariable(value = "catId",required = true)Long catId){
        System.out.println("controller======="+catId);
        List<AttrGroupVO> groupVos = this.attrGroupService.queryAttrByCId(catId);
        return Resp.ok(groupVos);
    }

    @ApiOperation("根据组id查询组下的所有属性")
    @GetMapping("/withattr/{gid}")
    public Resp<AttrGroupVO> queryAttrByGroupId(@PathVariable(value = "gid",required = true)Long gid){
        AttrGroupVO groupVo = this.attrGroupService.queryAttrByGroupId(gid);
        return Resp.ok(groupVo);
    }


    @ApiOperation("根据分类id分页查询数据")
    @GetMapping("/{catId}")
    public Resp<PageVo> queryByCidPage(@PathVariable(value = "catId",required = true)Long catId,QueryCondition queryCondition){

        PageVo page = attrGroupService.queryByCidPage(catId,queryCondition);

        return Resp.ok(page);
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:attrgroup:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = attrGroupService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{attrGroupId}")
    @PreAuthorize("hasAuthority('pms:attrgroup:info')")
    public Resp<AttrGroupEntity> info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        return Resp.ok(attrGroup);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:attrgroup:save')")
    public Resp<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:attrgroup:update')")
    public Resp<Object> update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:attrgroup:delete')")
    public Resp<Object> delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return Resp.ok(null);
    }

}
