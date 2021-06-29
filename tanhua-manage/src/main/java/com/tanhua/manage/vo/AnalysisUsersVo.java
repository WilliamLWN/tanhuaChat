package com.tanhua.manage.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisUsersVo {

    private List<Map<String, Object>> thisYear;

    private List<Map<String, Object>> lastYear;
}
