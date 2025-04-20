package com.example.tabletennis.mapper;

import com.example.tabletennis.entity.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeMapper {
    void insertLike(Long userId, Integer contentId);
    void deleteLike(Long userId, Integer contentId);
    boolean existsLike(Long userId, Integer contentId);
}