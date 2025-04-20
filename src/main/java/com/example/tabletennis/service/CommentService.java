package com.example.tabletennis.service;

import com.example.tabletennis.dto.CommentDTO;
import com.example.tabletennis.entity.Comment;
import com.example.tabletennis.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentMapper commentMapper;
    private final AuthService userService;
    private final NotificationService notificationService;

    public List<CommentDTO> getCommentsByContentId(Long contentId) {
        List<Comment> comments = commentMapper.selectByContentIdWithUsername(contentId);
        return buildCommentTree(comments);
    }

    private List<CommentDTO> buildCommentTree(List<Comment> comments) {
        Map<Long, CommentDTO> dtoMap = new HashMap<>();
        List<CommentDTO> rootComments = new ArrayList<>();

        for (Comment comment : comments) {
            CommentDTO dto = convertToDTO(comment);
            dtoMap.put(comment.getCommentId(), dto);

            if (comment.getParentId() == null) {
                rootComments.add(dto);
            } else {
                CommentDTO parentDto = dtoMap.get(comment.getParentId());
                if (parentDto != null) {
                    parentDto.getReplies().add(dto);
                }
            }
        }
        return rootComments;
    }

    private CommentDTO convertToDTO(Comment comment) {
        return new CommentDTO(
                comment.getCommentId(),
                comment.getContent(),
                comment.getUsername(),
                comment.getCreateTime(),
                new ArrayList<>()
        );
    }

    public CommentDTO addComment(Long contentId, Long userId, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setContentId(contentId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        String username = userService.getUsernameById(userId);
        notificationService.createCommentNotification(comment);
        return new CommentDTO(
                comment.getCommentId(),
                content,
                username,
                comment.getCreateTime(),
                new ArrayList<>()
        );
    }
}
