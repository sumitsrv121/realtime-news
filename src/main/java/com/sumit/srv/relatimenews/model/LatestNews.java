package com.sumit.srv.relatimenews.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LatestNews {
    private String title;
    private String text;
}
