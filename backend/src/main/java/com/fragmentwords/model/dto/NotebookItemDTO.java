package com.fragmentwords.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class NotebookItemDTO {
    private Long wordId;
    private String word;
    private String phonetic;
    private String translation;
    private String example;
    private Long vocabId;
    private String vocabName;
    private Date addedAt;
}
