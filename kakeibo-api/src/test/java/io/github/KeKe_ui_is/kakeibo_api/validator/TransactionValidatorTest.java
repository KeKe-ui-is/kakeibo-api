package io.github.KeKe_ui_is.kakeibo_api.validator;

import io.github.KeKe_ui_is.kakeibo_api.dto.request.TransactionRequest;
import io.github.KeKe_ui_is.kakeibo_api.exception.InvalidRequestException;
import io.github.KeKe_ui_is.kakeibo_api.model.ExpenseType;
import io.github.KeKe_ui_is.kakeibo_api.model.TransactionType;
import io.github.KeKe_ui_is.kakeibo_api.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionValidator テスト")
class TransactionValidatorTest {

    private static final Long USER_ID = 1L;
    private static final Long CATEGORY_ID = 2L;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionValidator transactionValidator;

    @Test
    @DisplayName("指定した有効なカテゴリが存在しない場合は入力エラーにする")
    void shouldRejectWhenActiveCategoryDoesNotExist() {
        TransactionRequest request = createRequest(null);
        when(transactionRepository.findCategoryTypeByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(null);

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> transactionValidator.validateCategoryAndExpenseType(USER_ID, request)
        );

        assertEquals("指定された有効なカテゴリが見つかりません", exception.getMessage());
    }

    @Test
    @DisplayName("収入カテゴリで固定費・変動費区分を指定しない場合は正常とする")
    void shouldAcceptIncomeWithoutExpenseType() {
        TransactionRequest request = createRequest(null);
        when(transactionRepository.findCategoryTypeByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(TransactionType.INCOME);

        assertDoesNotThrow(
                () -> transactionValidator.validateCategoryAndExpenseType(USER_ID, request)
        );
    }

    @Test
    @DisplayName("収入カテゴリで固定費・変動費区分を指定した場合は入力エラーにする")
    void shouldRejectIncomeWithExpenseType() {
        TransactionRequest request = createRequest(ExpenseType.FIXED);
        when(transactionRepository.findCategoryTypeByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(TransactionType.INCOME);

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> transactionValidator.validateCategoryAndExpenseType(USER_ID, request)
        );

        assertEquals("収入カテゴリでは固定費・変動費区分を指定できません", exception.getMessage());
    }

    @Test
    @DisplayName("支出カテゴリで固定費・変動費区分を指定した場合は正常とする")
    void shouldAcceptExpenseWithExpenseType() {
        TransactionRequest request = createRequest(ExpenseType.VARIABLE);
        when(transactionRepository.findCategoryTypeByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(TransactionType.EXPENSE);

        assertDoesNotThrow(
                () -> transactionValidator.validateCategoryAndExpenseType(USER_ID, request)
        );
    }

    @Test
    @DisplayName("支出カテゴリで固定費・変動費区分を指定しない場合は入力エラーにする")
    void shouldRejectExpenseWithoutExpenseType() {
        TransactionRequest request = createRequest(null);
        when(transactionRepository.findCategoryTypeByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(TransactionType.EXPENSE);

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> transactionValidator.validateCategoryAndExpenseType(USER_ID, request)
        );

        assertEquals("支出カテゴリでは固定費・変動費区分が必要です", exception.getMessage());
    }

    private TransactionRequest createRequest(ExpenseType expenseType) {
        TransactionRequest request = new TransactionRequest();
        request.setCategoryId(CATEGORY_ID);
        request.setExpenseType(expenseType);
        return request;
    }
}
