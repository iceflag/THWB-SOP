package com.zyd.sop.gatewaycommon.bean;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tanghc
 */
@Data
public class ConfigLimitDto {

    public static final byte LIMIT_STATUS_OPEN = 1;
    public static final byte LIMIT_STATUS_CLOSE = 0;

    /**  数据库字段：id */
    private Long id;

    /** 路由id, 数据库字段：route_id */
    private String routeId;

    /**  数据库字段：app_key */
    private String appKey;

    /** 限流ip，多个用英文逗号隔开, 数据库字段：limit_ip */
    private String limitIp;

    /** 服务id, 数据库字段：service_id */
    private String serviceId;

    /** 限流策略，1：漏桶策略，2：令牌桶策略, 数据库字段：limit_type */
    private Byte limitType;

    /** 每秒可处理请求数, 数据库字段：exec_count_per_second */
    private Integer execCountPerSecond;

    /** 返回的错误码, 数据库字段：limit_code */
    private String limitCode;

    /** 返回的错误信息, 数据库字段：limit_msg */
    private String limitMsg;

    /** 令牌桶容量, 数据库字段：token_bucket_count */
    private Integer tokenBucketCount;

    /** 限流开启状态，1:开启，0关闭, 数据库字段：limit_status */
    private Byte limitStatus;

    /** 顺序，值小的优先执行, 数据库字段：order_index */
    private Integer orderIndex;

    /**  数据库字段：gmt_create */
    private Date gmtCreate;

    /**  数据库字段：gmt_modified */
    private Date gmtModified;


    /**
     * 漏桶计数器
     */
    private LoadingCache<Long, AtomicLong> counter = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .build(new CacheLoader<Long, AtomicLong>() {
                @Override
                public AtomicLong load(Long seconds) throws Exception {
                    return new AtomicLong(0);
                }
            });

    /**
     * 令牌桶
     */
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private volatile RateLimiter rateLimiter;

    public synchronized void initRateLimiter() {
        rateLimiter = RateLimiter.create(tokenBucketCount);
    }

    /**
     * 获取令牌桶
     * @return
     */
    public RateLimiter fetchRateLimiter() {
        if (rateLimiter == null) {
            synchronized (this) {
                if (rateLimiter == null) {
                    rateLimiter = RateLimiter.create(tokenBucketCount);
                }
            }
        }
        return rateLimiter;
    }
}
