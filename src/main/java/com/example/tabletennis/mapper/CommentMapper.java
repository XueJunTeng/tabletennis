package com.example.tabletennis.mapper;

import com.example.tabletennis.entity.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectByContentIdWithUsername(@Param("contentId") Long contentId);

    int insert(Comment comment);

    Long selectUserIdByCommentId(Long parentId);
}