package com.sumit.srv.relatimenews.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestNewsToUser {
    private Long userId;
    private String title;
    private String text;
}
