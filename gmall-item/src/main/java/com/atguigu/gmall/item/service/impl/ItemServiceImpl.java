package com.atguigu.gmall.item.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.feign.GmallPmsFeign;
import com.atguigu.gmall.item.feign.GmallSmsFeign;
import com.atguigu.gmall.item.feign.GmallWmsFeign;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVO;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.sms.vo.ItemSaleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by FJC on 2019-11-14.
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallPmsFeign gmallPmsClient;

    @Autowired
    private GmallSmsFeign gmallSmsClient;

    @Autowired
    private GmallWmsFeign gmallWmsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    public ItemVO item(Long skuId) {

        ItemVO itemVO = new ItemVO();

        // 1. 查询sku信息
        //创建异步对象，使用线程池、可接收返回值的构造函数创建
        CompletableFuture<SkuInfoEntity> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            BeanUtils.copyProperties(skuInfoEntity, itemVO);
            return skuInfoEntity;
        }, threadPoolExecutor);
        //接收上一步骤的返回值(skuInfoEntity)，接收任务的处理结果，无返回值
        CompletableFuture<Void> brandCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 2.品牌
            Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandById(skuInfoEntity.getBrandId());
            itemVO.setBrand(brandEntityResp.getData());
        }, threadPoolExecutor);

        //接收第一步的返回值，自己本身没有返回值
        CompletableFuture<Void> categoryCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 3.分类
            Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
            itemVO.setCategory(categoryEntityResp.getData());
        }, threadPoolExecutor);

        //接收第一步的返回值，自己本身没有返回值
        CompletableFuture<Void> spuCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 4.spu信息
            Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuById(skuInfoEntity.getSpuId());
            itemVO.setSpuInfo(spuInfoEntityResp.getData());
        }, threadPoolExecutor);

        // 5.设置图片信息
        //另开一个异步编排，获取图片信息
        CompletableFuture<Void> picCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<String>> picsResp = this.gmallPmsClient.queryPicsBySkuId(skuId);
            itemVO.setPics(picsResp.getData());
        }, threadPoolExecutor);


        // 6.营销信息
        //另开一个异步编排，获取营销信息
        CompletableFuture<Void> saleCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<ItemSaleVO>> itemSaleResp = this.gmallSmsClient.queryItemSaleVOs(skuId);
            itemVO.setSales(itemSaleResp.getData());
        }, threadPoolExecutor);

        // 7.是否有货
        //另开一个异步编排，获取营销信息
        CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<WareSkuEntity>> wareSkuResp = this.gmallWmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
            itemVO.setStore(wareSkuEntities.stream().anyMatch(t -> t.getStock() > 0));
        }, threadPoolExecutor);

        // 8.spu所有的销售属性
        //使用第一步的异步编排结果，查询所有的营销信息，方法本身没有返回值
        CompletableFuture<Void> spuSaleCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = this.gmallPmsClient.querySaleAttrValues(skuInfoEntity.getSpuId());
            itemVO.setSkuSales(saleAttrValueResp.getData());
        }, threadPoolExecutor);

        // 9.spu的描述信息
        //使用第一步的异步编排结果，查询spu的描述信息，自己本身没有返回值
        CompletableFuture<Void> descCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsClient.querySpuDescById(skuInfoEntity.getSpuId());
            itemVO.setDesc(spuInfoDescEntityResp.getData());
        }, threadPoolExecutor);


        // 10.规格属性分组及组下的规格参数及值
        //使用第一步的异步编排结果，查询所有的规格参数组，以及规格名称和值，自己本身没有返回值
        CompletableFuture<Void> groupCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<List<GroupVO>> listResp = this.gmallPmsClient.queryGroupVOByCid(skuInfoEntity.getCatalogId(), skuInfoEntity.getSpuId());
            itemVO.setGroups(listResp.getData());
        }, threadPoolExecutor);

        //当所有的异步编排结果全部查询出来的时候，返回这个vo对象
        CompletableFuture.allOf(brandCompletableFuture, categoryCompletableFuture, spuCompletableFuture
                , picCompletableFuture, saleCompletableFuture, storeCompletableFuture, spuSaleCompletableFuture, descCompletableFuture, groupCompletableFuture).join();

        return itemVO;
    }

    public static void main(String[] args) {

        List<CompletableFuture<String>> completableFutures = Arrays.asList(CompletableFuture.completedFuture("hello"),
                CompletableFuture.completedFuture("world"),
                CompletableFuture.completedFuture("future"));
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[]{})).join();


//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("初始化CompletableFuture对象！");
////            int i = 1/0;
//            return "hello";
//        }).thenApply(t -> {
//            System.out.println("thenApply.......");
//            System.out.println("t....." + t);
//            return " thenApply";
//        }).whenCompleteAsync((t, u) -> {
//            System.out.println("whenCompleteAsync.......");
//            System.out.println("t....." + t);
//            System.out.println("u....." + u);
//        }).exceptionally(t -> {
//            System.out.println("exceptionally.......");
//            System.out.println("t....." + t);
//            return " exception";
//        }).handle((t, u) -> {
//            System.out.println("handle.......");
//            System.out.println("t....." + t);
//            System.out.println("u....." + u);
//            return " handler";
//        }).applyToEither(CompletableFuture.completedFuture("completedFuture"), (t) -> {
//            System.out.println("t: " + t);
//            System.out.println("两个线程完成后的一个新的业务逻辑");
//            return "thenCombine";
//        }).handle((t, u) -> {
//            System.out.println(t);
//            return "xxxx";
//        });
    }

//    public static void main(String[] args) {

//        new MyThread().start();
//
//        System.out.println("主线程执行。。。。");
//
//        System.out.println("=================================");

//        new Thread(() -> {
//            try {
//                TimeUnit.SECONDS.sleep(2);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("线程执行。。。。");
//        }).start();
//        System.out.println("主线程执行。。。。");

//        FutureTask<Object> futureTask = new FutureTask<>(() -> {
//            try {
//                TimeUnit.SECONDS.sleep(2);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("1处理子进程的业务逻辑。。。");
//            return "xxxx";
//        });
//        new Thread(futureTask).start();
//        while(futureTask.isDone()){
//
//        }
//        System.out.println("2主线程的业务逻辑。。。。");
//        try {
//            System.out.println("3" + futureTask.get());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        ExecutorService threadPool = Executors.newFixedThreadPool(3);
//
//        for (int i = 0; i < 10; i++) {
//            int finalI = i;
//            FutureTask<Object> futureTask = new FutureTask<>(() -> {
//                try {
//                    TimeUnit.SECONDS.sleep(2);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("处理子进程的业务逻辑。。。" + Thread.currentThread().getName());
//                return "xxxx";
//            });
//            threadPool.submit(futureTask);
//            System.out.println("主线程的业务逻辑。。。" + i);
//            try {
//                System.out.println(futureTask.get() + String.valueOf(i));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//        }


//    }
}

class MyThread extends Thread{

    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("线程执行。。。。");
    }
}
