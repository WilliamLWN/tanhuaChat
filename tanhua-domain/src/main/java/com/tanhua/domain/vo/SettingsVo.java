package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingsVo implements Serializable {

    private Long id;
    private String strangerQuestion = "";
    private String phone;
    private Boolean likeNotification = false;
    private Boolean pinglunNotification = false;
    private Boolean gonggaoNotification = false;

}
