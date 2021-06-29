package com.tanhua.manage.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  //实体类与json互转的时候 属性值为null的不参与序列化
public class AdminVo {
    private Long id;
    private String username;
    private String avatar;
}
