package com.tanhua.domain.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends BasePojo implements Serializable {

    private Long id;
    private String mobile; //手机号
    private String password; //密码

    //继承 BasePojo 自动填充后不需要这里写
//    private Date created;
//    private Date updated;
}
