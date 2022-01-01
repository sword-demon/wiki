package com.wx.wiki.mapper;

import com.wx.wiki.resp.StatisticResp;

import java.util.List;

public interface EbookSnapshotMapperCust {

    public void genSnapshot();

    public List<StatisticResp> getStatistic();
}
