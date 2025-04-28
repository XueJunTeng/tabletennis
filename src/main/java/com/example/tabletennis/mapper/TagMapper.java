package com.example.tabletennis.mapper;

import com.example.tabletennis.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TagMapper {
    Tag selectById(Integer tagId);  // 新增方法
    // 查询方法（无注解）
    List<Tag> selectTags(@Param("search") String search, @Param("pageSize") int pageSize);


    List<Integer> filterExistingTagIds(@Param("tagIds") List<Integer> tagIds);
    List<Tag> selectByKeyword(@Param("keyword") String keyword);
    int insert(Tag tag);
    int deleteById(Integer tagId);
    int deleteBatchIds(@Param("tagIds") List<Integer> tagIds);

    void updateById(Tag tag);
}