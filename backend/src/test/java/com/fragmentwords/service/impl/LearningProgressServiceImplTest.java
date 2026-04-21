package com.fragmentwords.service.impl;

import com.fragmentwords.common.ResourceNotFoundException;
import com.fragmentwords.mapper.LearningProgressMapper;
import com.fragmentwords.mapper.WordMapper;
import com.fragmentwords.model.dto.LearningDTO;
import com.fragmentwords.model.dto.NextWordDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningProgressServiceImplTest {

    @Mock
    private LearningProgressMapper learningProgressMapper;

    @Mock
    private WordMapper wordMapper;

    @InjectMocks
    private LearningProgressServiceImpl learningProgressService;

    @Test
    void getNextWordThrowsNotFoundWhenNoLearnableWordExists() {
        when(learningProgressMapper.findWordsToReview(null, null, null, 10))
            .thenReturn(Collections.emptyList());
        when(learningProgressMapper.findRandomNewWords(null, null, null, 10))
            .thenReturn(Collections.emptyList());
        when(learningProgressMapper.selectOne(any())).thenReturn(null);

        assertThrows(
            ResourceNotFoundException.class,
            () -> learningProgressService.getNextWord(null, null, new NextWordDTO())
        );
    }

    @Test
    void getWordProgressThrowsNotFoundWhenWordDoesNotExist() {
        when(learningProgressMapper.selectOne(any())).thenReturn(null);
        when(wordMapper.selectById(99L)).thenReturn(null);

        assertThrows(
            ResourceNotFoundException.class,
            () -> learningProgressService.getWordProgress("device-1", null, 99L)
        );
    }

    @Test
    void handleFeedbackRejectsMissingWordBeforePersistingProgress() {
        LearningDTO feedback = new LearningDTO();
        feedback.setWordId(7L);
        feedback.setIsKnown(Boolean.TRUE);

        when(wordMapper.selectById(7L)).thenReturn(null);

        assertThrows(
            ResourceNotFoundException.class,
            () -> learningProgressService.handleFeedback("device-1", null, feedback)
        );

        verify(learningProgressMapper, never()).selectOne(any());
        verify(learningProgressMapper, never()).insert(any());
        verify(learningProgressMapper, never()).updateById(any());
    }
}
