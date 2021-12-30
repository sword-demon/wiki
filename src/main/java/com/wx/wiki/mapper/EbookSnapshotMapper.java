package com.wx.wiki.mapper;

import com.wx.wiki.domain.EbookSnapshot;
import com.wx.wiki.domain.EbookSnapshotExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EbookSnapshotMapper {
    long countByExample(EbookSnapshotExample example);

    int deleteByExample(EbookSnapshotExample example);

    int deleteByPrimaryKey(Long id);

    int insert(EbookSnapshot record);

    int insertSelective(EbookSnapshot record);

    List<EbookSnapshot> selectByExample(EbookSnapshotExample example);

    EbookSnapshot selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") EbookSnapshot record, @Param("example") EbookSnapshotExample example);

    int updateByExample(@Param("record") EbookSnapshot record, @Param("example") EbookSnapshotExample example);

    int updateByPrimaryKeySelective(EbookSnapshot record);

    int updateByPrimaryKey(EbookSnapshot record);
}