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
            @RequestParam(defaultValue = "1000") int pageSize
    ) {
        return ResponseEntity.ok(tagService.getTags(search, pageSize));
    }

}