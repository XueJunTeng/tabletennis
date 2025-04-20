package com.example.tabletennis.controller;

import com.example.tabletennis.entity.Tag;
import com.example.tabletennis.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    // 获取所有标签
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "50") int pageSize
    ) {
        return ResponseEntity.ok(tagService.getTags(search, pageSize));
    }

    // 获取热门标签
    @GetMapping("/hot")
    public ResponseEntity<List<Tag>> getHotTags() {
        return ResponseEntity.ok(tagService.getHotTags(10));
    }

    // 创建新标签
    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
        return ResponseEntity.ok(tagService.createTag(tag));
    }
}