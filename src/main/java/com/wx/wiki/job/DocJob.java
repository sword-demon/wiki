package com.wx.wiki.job;

import com.wx.wiki.service.DocService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /**
     * 每30秒更新电子书信息
     */
    @Scheduled(cron = "5/30 * * * * ? ")
    public void cron() {
//        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
//        String dateString = format.format(new Date());
//        Thread.sleep(1500);
//        LOG.info("每隔1.5秒执种执行一次: {}", dateString);

        docService.updateEbookInfo();
    }
}
