package com.wx.wiki.job;

import com.wx.wiki.service.DocService;
import com.wx.wiki.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DocJob {

    private static final Logger LOG = LoggerFactory.getLogger(DocJob.class);

    @Resource
    private DocService docService;

    @Resource
    private SnowFlake snowFlake;

    /**
     * 每30秒更新电子书信息
     */
    @Scheduled(cron = "5/30 * * * * ? ")
    public void cron() {
        // 增加日志流水号
        MDC.put("LOG_ID", String.valueOf(snowFlake.nextId()));
//        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
//        String dateString = format.format(new Date());
//        Thread.sleep(1500);
//        LOG.info("每隔1.5秒执种执行一次: {}", dateString);

        LOG.info("更新电子书下的文档数据开始");
        long startTime = System.currentTimeMillis();
        docService.updateEbookInfo();
        LOG.info("更新电子书下的文档数据结束，耗时: {} 毫秒", System.currentTimeMillis() - startTime);
    }
}
