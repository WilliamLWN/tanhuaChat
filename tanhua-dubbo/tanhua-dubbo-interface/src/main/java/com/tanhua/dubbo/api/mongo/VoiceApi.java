package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Voice;

import java.util.List;
import java.util.Map;

public interface VoiceApi {
    void save(Voice voice);

    Map<String, Object> findVoice(Long userId);

    void updateById(Voice voice);
}
