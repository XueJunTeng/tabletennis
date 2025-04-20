// ContentUploadDTO.java
package com.example.tabletennis.dto;

import com.example.tabletennis.enums.ContentType;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class ContentUploadDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100字")
    private String title;

    private String description;

    @NotNull(message = "内容类型不能为空")
    private ContentType type;

    @Size(max = 5, message = "最多选择5个标签")
    private List<Integer> tagIds;

}