package io.github.KeKe_ui_is.kakeibo_api.validator;

import io.github.KeKe_ui_is.kakeibo_api.exception.ConflictException;
import io.github.KeKe_ui_is.kakeibo_api.model.TransactionType;
import io.github.KeKe_ui_is.kakeibo_api.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryValidator テスト")
class CategoryValidatorTest {

    private static final Long USER_ID = 1L;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryValidator categoryValidator;

    @Test
    @DisplayName("新規登録時に同名かつ同じ収支区分のカテゴリがなければ正常とする")
    void shouldAcceptWhenDuplicateDoesNotExistOnCreate() {
        when(categoryRepository.countByNameAndType(USER_ID, "食費", TransactionType.EXPENSE))
                .thenReturn(0);

        assertDoesNotThrow(() -> categoryValidator.validateDuplicate(
                USER_ID,
                "食費",
                TransactionType.EXPENSE,
                null
        ));

        verify(categoryRepository, never()).countByNameAndTypeExcludingId(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    @DisplayName("新規登録時に同名かつ同じ収支区分のカテゴリがある場合は競合エラーにする")
    void shouldRejectDuplicateOnCreate() {
        when(categoryRepository.countByNameAndType(USER_ID, "食費", TransactionType.EXPENSE))
                .thenReturn(1);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> categoryValidator.validateDuplicate(
                        USER_ID,
                        "食費",
                        TransactionType.EXPENSE,
                        null
                )
        );

        assertEquals("同じ名前と収支区分のカテゴリがすでに存在します", exception.getMessage());
    }

    @Test
    @DisplayName("更新時は更新対象IDを除外して重複確認する")
    void shouldExcludeCurrentCategoryWhenCheckingDuplicateOnUpdate() {
        Long categoryId = 10L;
        when(categoryRepository.countByNameAndTypeExcludingId(
                USER_ID,
                "食費",
                TransactionType.EXPENSE,
                categoryId
        )).thenReturn(0);

        assertDoesNotThrow(() -> categoryValidator.validateDuplicate(
                USER_ID,
                "食費",
                TransactionType.EXPENSE,
                categoryId
        ));

        verify(categoryRepository).countByNameAndTypeExcludingId(
                USER_ID,
                "食費",
                TransactionType.EXPENSE,
                categoryId
        );
    }

    @Test
    @DisplayName("収支から使用されていないカテゴリは削除可能とする")
    void shouldAllowDeletionWhenCategoryIsUnused() {
        when(categoryRepository.countTransactions(10L, USER_ID)).thenReturn(0);

        assertDoesNotThrow(() -> categoryValidator.validateDeletable(USER_ID, 10L));
    }

    @Test
    @DisplayName("収支から使用されているカテゴリは削除不可として競合エラーにする")
    void shouldRejectDeletionWhenCategoryIsUsed() {
        when(categoryRepository.countTransactions(10L, USER_ID)).thenReturn(2);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> categoryValidator.validateDeletable(USER_ID, 10L)
        );

        assertEquals("収支で使用されているカテゴリは削除できません。無効化してください", exception.getMessage());
    }
}
