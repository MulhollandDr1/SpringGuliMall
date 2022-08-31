package com.example.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.example.gulimall.product.dao.CategoryBrandRelationDao;
import com.example.gulimall.product.entity.CategoryBrandRelationEntity;
import com.example.gulimall.product.vo.Category2Vo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.product.dao.CategoryDao;
import com.example.gulimall.product.entity.CategoryEntity;
import com.example.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryBrandRelationDao categoryBrandRelationDao;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        List<CategoryEntity> Level1Menu = categoryEntities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0).map(categoryEntity -> {
            categoryEntity.setChildren(getChildren(categoryEntity, categoryEntities));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return Level1Menu;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1、检查当前删除的菜单，是否被别的地方调用
        baseMapper.deleteBatchIds(asList);
        categoryBrandRelationDao.delete(new QueryWrapper<CategoryBrandRelationEntity>().in("catelog_id", asList));
    }

    @Override
    public Long[] findCategoryPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, path);
        Collections.reverse(parentPath);
        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }
    /*清除指定缓存区的缓存*/
/*    @Caching(evict = {
            @CacheEvict(value = "category",key = "'getFirstLevel'"),
            @CacheEvict(value = "category",key = "'getCatalogJson'")
    })*/
    @CacheEvict(value = "category",allEntries = true) /*清除指定缓存区的所有缓存*/
    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        if (StringUtils.isNotEmpty(category.getName())) {
            CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
            categoryBrandRelationEntity.setCatelogName(category.getName());
            categoryBrandRelationDao.update(
                    categoryBrandRelationEntity,
                    new UpdateWrapper<CategoryBrandRelationEntity>().eq("catelog_id", category.getCatId())
            );
        }
//        TODO 关联其他分类
    }
    @Cacheable(value = "category",key = "#root.method.name",sync = true) /*value指定缓存的分区*/
    @Override
    public List<CategoryEntity> getFirstLevel() {
        System.out.println("getFirstLevel");
        return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
    }
    @Cacheable(value = "category",key = "#root.methodName",sync = true) /*sync，spring-cache的本地锁，用来应对读多写少的场景已经足够*/
    @Override
    public Map<String, List<Category2Vo>> getCatalogJson() {
        return getCatalogJsonFromDb();
    }

    public Map<String, List<Category2Vo>> getCatalogJsonWithStringRedisTemplate() {
        /*先从缓存中读取指定数据*/
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String catalogJson = ops.get("catalogJson");
        /*如果没有，从数据库中读取，存到缓存中，再返回数据*/
        if (StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Category2Vo>> catalogJsonFromDb = this.getCatalogJsonFromDbWithRedissonLock();
            return catalogJsonFromDb;
        }
        /*如果有，需要把从缓存中读取的json字符串转换成需要的对象类型*/
        System.out.println("缓存命中");
        Map<String, List<Category2Vo>> map = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Category2Vo>>>() {
        });
        return map;
    }
    public Map<String, List<Category2Vo>> getCatalogJsonFromDbWithRedissonLock(){
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock(30,TimeUnit.SECONDS); /*阻塞式等待*/
        Map<String, List<Category2Vo>> catalogJsonFromDb;
        try {
            catalogJsonFromDb = getCatalogJsonFromDbTest();
        }finally {
            lock.unlock();
        }
        return catalogJsonFromDb;
    }

    public Map<String, List<Category2Vo>> getCatalogJsonFromDb() {
        System.out.println("查询了一次数据库");
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);
        List<CategoryEntity> firstLevels = categoryEntities.stream().filter(categoryEntity -> categoryEntity.getCatLevel() == 1).collect(Collectors.toList());
        Map<String, List<Category2Vo>> collect = firstLevels.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> secondLevels = getParent_cid(categoryEntities, v.getCatId());
            List<Category2Vo> category2Vos = null;
            if (CollectionUtils.isNotEmpty(secondLevels)) {
                category2Vos = secondLevels.stream().map(
                        secondLevel -> {
                            List<Category2Vo.Category3Vo> thirdLevelList = null;
                            List<CategoryEntity> thirdLevels = getParent_cid(categoryEntities, secondLevel.getCatId());
                            if (CollectionUtils.isNotEmpty(thirdLevels)) {
                                thirdLevelList = thirdLevels.stream().map(
                                        thirdLevel -> new Category2Vo.Category3Vo(secondLevel.getCatId().toString(), thirdLevel.getCatId().toString(), thirdLevel.getName())
                                ).collect(Collectors.toList());
                            }
                            return new Category2Vo(v.getCatId().toString(), thirdLevelList, secondLevel.getCatId().toString(), secondLevel.getName());
                        }
                ).collect(Collectors.toList());
            }
            return category2Vos;
        }));
        return collect;
    }

    public Map<String, List<Category2Vo>> getCatalogJsonFromDbWithRedisLock() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String uuid = UUID.randomUUID().toString();
        /*设置锁和过期时间*/
        System.out.println("尝试设置锁");
        Boolean lock = ops.setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        /*如果设置成功，表明之前没有上锁*/
        if (lock) {
            Map<String, List<Category2Vo>> catalogJsonFromDb;
            System.out.println("已设置锁");
            try {
                catalogJsonFromDb = getCatalogJsonFromDbTest();
            } finally {
                /*删除成功返回1，失败返回0，类型为Long*/
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1])else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);
                System.out.println("已删除锁");
            }
            return catalogJsonFromDb;
            /*            *//*防止业务超时，导致锁过期，当前线程删除的是其他线程的锁*//*
             *//*此处获取锁和删除锁不是同时进行，不用此方法*//*
            if(ops.get("lock").equals(uuid)){
                stringRedisTemplate.delete("lock");
            }*/
        } else {
            /*设置锁失败，继续尝试设置锁*/
            try {
                Thread.sleep(300);
            } catch (Exception e) {
                log.error(e.toString());
            }
            return getCatalogJsonFromDbWithLocalLock();
        }
    }

    public Map<String, List<Category2Vo>> getCatalogJsonFromDbWithLocalLock() {
        synchronized (this) {
            return getCatalogJsonFromDbTest();
        }
    }

    private Map<String, List<Category2Vo>> getCatalogJsonFromDbTest() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        /*防止数据存到缓存，释放锁后，其他线程再次查询数据库，需要在锁里面再次判断缓存中是否有数据*/
        if (StringUtils.isNotEmpty(ops.get("catalogJson"))) {
            System.out.println("缓存命中");
            return JSON.parseObject(ops.get("catalogJson"), new TypeReference<Map<String, List<Category2Vo>>>() {
            });
        }
        System.out.println("查询了一次数据库");
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);
        List<CategoryEntity> firstLevels = categoryEntities.stream().filter(categoryEntity -> categoryEntity.getCatLevel() == 1).collect(Collectors.toList());
        Map<String, List<Category2Vo>> collect = firstLevels.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> secondLevels = getParent_cid(categoryEntities, v.getCatId());
            List<Category2Vo> category2Vos = null;
            if (CollectionUtils.isNotEmpty(secondLevels)) {
                category2Vos = secondLevels.stream().map(
                        secondLevel -> {
                            List<Category2Vo.Category3Vo> thirdLevelList = null;
                            List<CategoryEntity> thirdLevels = getParent_cid(categoryEntities, secondLevel.getCatId());
                            if (CollectionUtils.isNotEmpty(thirdLevels)) {
                                thirdLevelList = thirdLevels.stream().map(
                                        thirdLevel -> new Category2Vo.Category3Vo(secondLevel.getCatId().toString(), thirdLevel.getCatId().toString(), thirdLevel.getName())
                                ).collect(Collectors.toList());
                            }
                            return new Category2Vo(v.getCatId().toString(), thirdLevelList, secondLevel.getCatId().toString(), secondLevel.getName());
                        }
                ).collect(Collectors.toList());
            }
            return category2Vos;
        }));
        /*在释放锁之前把数据加入缓存*/
        /*存入缓存之前，先要把数据转换成json字符串*/
        ops.set("catalogJson", JSON.toJSONString(collect), 1, TimeUnit.DAYS);
        return collect;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> categoryEntities, Long parentCid) {
        List<CategoryEntity> collect = categoryEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return collect;
        /*        return this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));*/
    }

    public List<Long> findParentPath(Long catelogId, List<Long> path) {
        path.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), path);
        }
        return path;
    }

    public List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == root.getCatId()).map(categoryEntity -> {
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

}