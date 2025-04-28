package com.example.tabletennis.controller;

import com.example.tabletennis.entity.Tag;
import com.example.tabletennis.service.TagService;
import com.github.pagehelper.PageInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tags")
@PreAuthorize("hasRole('ADMIN')")
public class TagAdminController {
    private final TagService tagService;

    public TagAdminController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<PageInfo<Tag>> getTags(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(tagService.getTags(page, pageSize, keyword));
    }

    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Map<String, Object> request) {
        String tagName = (String) request.get("tagName");
        Integer weight = (Integer) request.get("weight");
        Tag createdTag = tagService.createTag(tagName, weight);
        return ResponseEntity.created(URI.create("/api/admin/tags/" + createdTag.getTagId()))
                .body(createdTag);
    }

    @PutMapping("/{tagId}/weight")
    public ResponseEntity<Tag> updateTagWeight(
            @PathVariable Integer tagId,
            @RequestBody Map<String, Integer> request) {
        Integer newWeight = request.get("weight");
        Tag updatedTag = tagService.updateTagWeight(tagId, newWeight);
        return ResponseEntity.ok(updatedTag);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Integer tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<Void> batchDeleteTags(@RequestBody Map<String, List<Integer>> request) {
        tagService.batchDeleteTags(request.get("tagIds"));
        return ResponseEntity.noContent().build();
    }


}