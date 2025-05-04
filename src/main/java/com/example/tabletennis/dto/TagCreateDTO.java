package com.example.tabletennis.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagCreateDTO {
    @NotBlank(message = "标签名称不能为空")
    private String tagName;

    @Min(value = 0, message = "权重值不能小于0")
    private Integer weight;

    // 省略getter/setter
}