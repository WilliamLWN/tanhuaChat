package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.CommentVideo;
import com.tanhua.domain.vo.PageResult;

public interface CommentApi {

    long save(Comment comment);

    long delete(Comment comment);

    PageResult queryCommentsList(String publishId, Integer page, Integer pagesize);

    void saveComment(Comment c);

    PageResult findLikesByUserId(Integer page, Integer pagesize, Long userId);

    PageResult findCommentsByUserId(Integer page, Integer pagesize, Long userId);

    PageResult findLovesByUserId(Integer page, Integer pagesize, Long userId);

    long deleteVideoLike(CommentVideo commentVideo);

    PageResult queryVideoCommentsList(String videoId, Integer page, Integer pagesize);

    long saveVideolikeComment(CommentVideo commentVideo);

    void saveVideoComment(CommentVideo c);
}
