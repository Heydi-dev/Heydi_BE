package com.example.heydibe.community.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostCreateResponse {

    private Long post_id;
    private Long user_id;
    private Long diary_id;
    private String post_title;
    private LocalDate diary_date;
    private List<String> post_topics;
    private String post_emotion;
    private String post_content;
    private List<Photo> photos;
    private int like_count;
    private int comment_count;
    private boolean is_liked;
    private LocalDateTime created_at;

    @Getter
    @AllArgsConstructor
    public static class Photo {
        private Long id;
        private String image_url;
    }
}
