package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 环信通讯的用户名和密码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HuanXinUser {

    private String username;
    private String password;
}
