package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Question;

public interface QuestionApi {

    Question findByUserId(Long userId);

    void save(Question question);

    void update(Question question);
}
