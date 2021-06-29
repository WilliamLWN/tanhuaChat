package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceVo implements Serializable {

    private Long id;  //用户id
    private String avatar; //头像
    private String nickname; //昵称
    private String gender; //性别
    private Integer age; //年龄
    private String soundUrl; //音频地址
    private Integer remainingTimes; //剩余次数
}
