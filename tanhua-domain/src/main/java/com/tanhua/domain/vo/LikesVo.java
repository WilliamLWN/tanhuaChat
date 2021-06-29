package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikesVo implements Serializable {

    private String id;
    private String avatar;
    private String nickname;
    private String createDate;
}

