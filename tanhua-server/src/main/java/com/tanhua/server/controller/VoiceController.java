package com.tanhua.server.controller;

import com.tanhua.server.service.VoiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
public class VoiceController {

    @Autowired
    private VoiceService voiceService;


    /**
     * 接口名称：桃花传音-发送语音
     * 接口路径：POST/peachblossom
     */
    @PostMapping("/peachblossom")
    public ResponseEntity<Object> sendpeachblossom(MultipartFile soundFile) throws IOException {
        log.info("接口名称：桃花传音-发送语音");
        return voiceService.sendpeachblossom(soundFile);
    }

    /**
     * 接口名称：桃花传音-接收语音
     * 接口路径：GET/peachblossom
     */
    @GetMapping("/peachblossom")
    public ResponseEntity<Object> peachblossom() {
        log.info("接口名称：桃花传音-接收语音");
        return voiceService.peachblossom();
    }
}
