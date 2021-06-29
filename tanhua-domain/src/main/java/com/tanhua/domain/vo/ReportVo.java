package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportVo {

    private String conclusion; //结论
    private String cover; //对应图片
    private List<Map<String,String>> dimensions; //维度
    private List<Map<String,Object>> similarYou; //与你相似
}
