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

    @Transactional
    public Tag createTag(String tagName, Integer weight) {
        Tag tag = new Tag();
        tag.setTagName(tagName);
        tag.setWeight(Float.valueOf(weight));// 保存到数据库的实现
        tagMapper.insert(tag);
        return tag;
    }

    public Tag updateTagWeight(Integer tagId, Integer newWeight) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag != null) {
            tag.setWeight(Float.valueOf(newWeight));
            tagMapper.updateById(tag);
        }
        return tag;
    }
    public PageInfo<Tag> getTags(int page, int pageSize, String keyword) {
        PageHelper.startPage(page, pageSize);
        List<Tag> tags = tagMapper.selectByKeyword(keyword);
        return new PageInfo<>(tags);
    }

    public void deleteTag(Integer tagId) {
        tagMapper.deleteById(tagId);
    }

    public void batchDeleteTags(List<Integer> tagIds) {
        tagMapper.deleteBatchIds(tagIds);
    }
}