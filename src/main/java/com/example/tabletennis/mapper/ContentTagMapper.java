// ContentTagMapper.java
package com.example.tabletennis.mapper;

import com.example.tabletennis.entity.ContentTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ContentTagMapper {
    /**
     * 批量插入内容-标签关联关系
     * @param contentId 内容ID
     * @param tagIds 标签ID列表
     */
    void batchInsert(
            @Param("contentId") Integer contentId,
            @Param("tagIds") List<Integer> tagIds
    );

    /**
     * 删除指定内容的所有标签关联
     * @param contentId 内容ID
     */
    void deleteByContentId(Integer contentId);

    List<ContentTag> selectByContentId(Integer contentId);
}