package com.example.tabletennis.mapper;
import com.github.pagehelper.Page;
import com.example.tabletennis.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface ContentMapper {

    //插入新的内容（视频/文章）
    int insertContent(Content content);

    //根据内容ID更新内容状态（如审核通过/拒绝）
    void updateContentStatus(Integer contentId, String status);

    //根据内容ID查询内容详情
    Content selectContentById(Integer contentId);

    //根据用户ID查询其上传的所有内容
    List<Content> selectContentsByUserId(Integer userId);

    //查询所有通过审核的内容
    List<Content> selectApprovedVideos();

    // 增加点赞数
    int incrementLikeCount(Integer contentId);

    // 减少点赞数
    int decrementLikeCount(Integer contentId);

    // 查询点赞数
    int getLikeCountByContentId(Integer contentId);

    // 增加收藏数
    void incrementFavoriteCount(Integer contentId);

    // 减少收藏数
    void decrementFavoriteCount(Integer contentId);

    // 获取收藏数
    int getFavoriteCount(Integer contentId);

    //浏览量增加
    void incrementViewCount(Integer contentId);

    //查询所有通过审核的内容
    List<Content> selectApprovedContents();

    Page<Content> selectByStatusWithPage(@Param("status") String status);

    int updateStatusWithNote(@Param("contentId") Integer contentId,
                             @Param("status") String status,
                             @Param("reviewNotes") String reviewNotes);

    Content selectById(@Param("contentId") Integer contentId);
    Long selectUserIdByContentId(@Param("contentId") Integer contentId);
    Long selectAuthorIdByContentId(Integer contentId);

    Page<Content> selectByUsertypeWithPage(@Param("userId")Long userId,@Param("behaviorType")String behaviorType);

    List<Content> selectApprovedArticles();

    List<Content> selectByTitle(@Param("title") String title);
    // 添加批量查询方法
    List<Content> selectBatchIds(@Param("contentIds") List<Integer> contentIds);
}