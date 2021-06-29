package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Settings;

public interface SettingsApi {

    Settings findByUserId(Long userId);

    void save(Settings settings);

    void update(Settings settings);
}
