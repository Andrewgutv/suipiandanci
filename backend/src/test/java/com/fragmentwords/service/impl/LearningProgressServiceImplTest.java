package com.fragmentwords.service.impl;

import com.fragmentwords.common.ResourceNotFoundException;
import com.fragmentwords.mapper.LearningProgressMapper;
import com.fragmentwords.mapper.WordMapper;
import com.fragmentwords.model.dto.LearningDTO;
import com.fragmentwords.model.dto.LearningResponseDTO;
import com.fragmentwords.model.dto.NextWordDTO;
import com.fragmentwords.model.entity.Word;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void getNextWordReturnsPreviewForNewWordWithoutPersistingProgress() {
        Word word = new Word();
        word.setId(7L);
        word.setWord("banana");
        word.setTranslation("n. 香蕉");

        when(learningProgressMapper.findWordsToReview("device-1", null, null, 10, null))
            .thenReturn(Collections.emptyList());
        when(learningProgressMapper.findRandomNewWords("device-1", null, null, 10, null))
            .thenReturn(List.of(7L));
        when(wordMapper.selectById(7L)).thenReturn(word);

        LearningResponseDTO response = learningProgressService.getNextWord("device-1", null, new NextWordDTO());

        assertEquals(7L, response.getWordId());
        assertEquals("banana", response.getWord());
        assertEquals(0, response.getStage());
        assertFalse(response.getIsMastered());
        assertTrue(response.getNextReviewTime() == null);
        verify(learningProgressMapper, never()).insert(any());
        verify(learningProgressMapper, never()).updateById(any());
    }

    @Test
    void getNextWordThrowsNotFoundWhenNoLearnableWordExists() {
        when(learningProgressMapper.findWordsToReview(null, null, null, 10, null))
            .thenReturn(Collections.emptyList());
        when(learningProgressMapper.findRandomNewWords(null, null, null, 10, null))
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
