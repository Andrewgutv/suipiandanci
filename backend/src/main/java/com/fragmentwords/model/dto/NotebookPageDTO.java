package com.fragmentwords.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NotebookPageDTO {
    private Integer pageNum;
    private Integer pageSize;
    private Long total;
    private List<NotebookItemDTO> items;
}
