package com.example.tabletennis.service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.example.tabletennis.entity.Tag;
import com.example.tabletennis.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagMapper tagMapper;

    @Transactional(readOnly = true)
    public List<Tag> getTags(String search, int pageSize) {
        return tagMapper.selectTags("%" + search + "%", pageSize);
    }

    @Transactional(readOnly = true)
    public List<Tag> getHotTags(int limit) {
        return tagMapper.selectHotTags(limit);
    }

    @Transactional
    public Tag createTag(Tag tag) {
        tagMapper.insertTag(tag);
        return tag;
    }

    public PageInfo<Tag> getTags(int page, int pageSize, String keyword) {
        PageHelper.startPage(page, pageSize);
        List<Tag> tags = tagMapper.selectByKeyword(keyword);
        return new PageInfo<>(tags);
    }

    public Tag createTag(String tagName) {
        Tag tag = new Tag();
        tag.setTagName(tagName);
        tagMapper.insert(tag);
        return tag;
    }

    public void deleteTag(Integer tagId) {
        tagMapper.deleteById(tagId);
    }

    public void batchDeleteTags(List<Integer> tagIds) {
        tagMapper.deleteBatchIds(tagIds);
    }
}