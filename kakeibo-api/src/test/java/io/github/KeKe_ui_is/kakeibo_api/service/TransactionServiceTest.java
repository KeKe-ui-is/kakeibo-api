package io.github.KeKe_ui_is.kakeibo_api.service;

import io.github.KeKe_ui_is.kakeibo_api.dto.request.TransactionRequest;
import io.github.KeKe_ui_is.kakeibo_api.dto.response.TransactionResponse;
import io.github.KeKe_ui_is.kakeibo_api.model.ExpenseType;
import io.github.KeKe_ui_is.kakeibo_api.model.Transaction;
import io.github.KeKe_ui_is.kakeibo_api.repository.TransactionRepository;
import io.github.KeKe_ui_is.kakeibo_api.validator.TransactionValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService テスト")
class TransactionServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionValidator transactionValidator;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("全件取得ではユーザーIDに紐づく収支一覧を返す")
    void shouldReturnAllTransactionsForUser() {
        List<TransactionResponse> expected = List.of(createResponse(1L), createResponse(2L));
        when(transactionRepository.findAllByUserId(USER_ID)).thenReturn(expected);

        List<TransactionResponse> actual = transactionService.findAll(USER_ID);

        assertSame(expected, actual);
        verify(transactionRepository).findAllByUserId(USER_ID);
    }

    @Test
    @DisplayName("IDに一致する収支が存在する場合はその収支を返す")
    void shouldReturnTransactionWhenTransactionExists() {
        TransactionResponse expected = createResponse(10L);
        when(transactionRepository.findByIdAndUserId(10L, USER_ID)).thenReturn(expected);

        TransactionResponse actual = transactionService.findById(USER_ID, 10L);

        assertSame(expected, actual);
        verify(transactionRepository).findByIdAndUserId(10L, USER_ID);
    }

    @Test
    @DisplayName("IDに一致する収支が存在しない場合は404例外を投げる")
    void shouldThrowNotFoundWhenTransactionDoesNotExist() {
        when(transactionRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.findById(USER_ID, 99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("収支が見つかりません", exception.getReason());
    }

    @Test
    @DisplayName("指定した年月の1日から翌月1日未満を検索期間として使用する")
    void shouldUseFirstDayAndNextMonthAsSearchPeriod() {
        YearMonth yearMonth = YearMonth.of(2026, 9);
        List<TransactionResponse> expected = List.of(createResponse(1L));
        when(transactionRepository.findByUserIdAndPeriod(
                USER_ID,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 1)
        )).thenReturn(expected);

        List<TransactionResponse> actual = transactionService.findByMonth(USER_ID, yearMonth);

        assertSame(expected, actual);
        verify(transactionRepository).findByUserIdAndPeriod(
                USER_ID,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 1)
        );
    }

    @Test
    @DisplayName("12月を指定した場合は翌年1月1日を終了日として使用する")
    void shouldUseJanuaryOfNextYearAsEndDateWhenDecemberIsSpecified() {
        YearMonth yearMonth = YearMonth.of(2026, 12);
        when(transactionRepository.findByUserIdAndPeriod(
                USER_ID,
                LocalDate.of(2026, 12, 1),
                LocalDate.of(2027, 1, 1)
        )).thenReturn(List.of());

        List<TransactionResponse> actual = transactionService.findByMonth(USER_ID, yearMonth);

        assertEquals(List.of(), actual);
        verify(transactionRepository).findByUserIdAndPeriod(
                USER_ID,
                LocalDate.of(2026, 12, 1),
                LocalDate.of(2027, 1, 1)
        );
    }

    @Test
    @DisplayName("収支登録時は入力値をTransactionへ詰め替えて保存し登録後の収支を返す")
    void shouldCreateTransactionAndReturnCreatedTransaction() {
        TransactionRequest request = createRequest();
        TransactionResponse expected = createResponse(100L);

        doAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(100L);
            return 1;
        }).when(transactionRepository).insert(any(Transaction.class));
        when(transactionRepository.findByIdAndUserId(100L, USER_ID)).thenReturn(expected);

        TransactionResponse actual = transactionService.create(USER_ID, request);

        assertSame(expected, actual);
        verify(transactionValidator).validateCategoryAndExpenseType(USER_ID, request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).insert(captor.capture());
        Transaction saved = captor.getValue();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(request.getCategoryId(), saved.getCategoryId());
        assertEquals(request.getAmount(), saved.getAmount());
        assertEquals(request.getTransactionDate(), saved.getTransactionDate());
        assertEquals(request.getExpenseType(), saved.getExpenseType());
        assertEquals(request.getMemo(), saved.getMemo());
    }

    @Test
    @DisplayName("収支更新時は存在確認と入力チェック後に更新し更新後の収支を返す")
    void shouldUpdateTransactionAndReturnUpdatedTransaction() {
        Long transactionId = 10L;
        TransactionRequest request = createRequest();
        TransactionResponse existing = createResponse(transactionId);
        TransactionResponse updated = createResponse(transactionId);
        updated.setAmount(request.getAmount());

        when(transactionRepository.findByIdAndUserId(transactionId, USER_ID))
                .thenReturn(existing, updated);

        TransactionResponse actual = transactionService.update(USER_ID, transactionId, request);

        assertSame(updated, actual);
        verify(transactionValidator).validateCategoryAndExpenseType(USER_ID, request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).update(captor.capture());
        Transaction saved = captor.getValue();
        assertEquals(transactionId, saved.getId());
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(request.getCategoryId(), saved.getCategoryId());
        assertEquals(request.getAmount(), saved.getAmount());
        assertEquals(request.getTransactionDate(), saved.getTransactionDate());
        assertEquals(request.getExpenseType(), saved.getExpenseType());
        assertEquals(request.getMemo(), saved.getMemo());
    }

    @Test
    @DisplayName("存在しない収支を更新する場合は更新処理を行わず404例外を投げる")
    void shouldNotUpdateWhenTransactionDoesNotExist() {
        when(transactionRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.update(USER_ID, 99L, createRequest())
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(transactionValidator, never()).validateCategoryAndExpenseType(any(), any());
        verify(transactionRepository, never()).update(any(Transaction.class));
    }

    @Test
    @DisplayName("収支を1件削除できた場合は正常終了する")
    void shouldDeleteTransactionWhenTransactionExists() {
        when(transactionRepository.deleteByIdAndUserId(10L, USER_ID)).thenReturn(1);

        transactionService.delete(USER_ID, 10L);

        verify(transactionRepository).deleteByIdAndUserId(10L, USER_ID);
    }

    @Test
    @DisplayName("削除対象の収支が存在しない場合は404例外を投げる")
    void shouldThrowNotFoundWhenDeleteTargetDoesNotExist() {
        when(transactionRepository.deleteByIdAndUserId(99L, USER_ID)).thenReturn(0);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.delete(USER_ID, 99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("収支が見つかりません", exception.getReason());
    }

    private TransactionRequest createRequest() {
        TransactionRequest request = new TransactionRequest();
        request.setCategoryId(2L);
        request.setAmount(3_500L);
        request.setTransactionDate(LocalDate.of(2026, 9, 6));
        request.setExpenseType(ExpenseType.VARIABLE);
        request.setMemo("昼食");
        return request;
    }

    private TransactionResponse createResponse(Long id) {
        TransactionResponse response = new TransactionResponse();
        response.setId(id);
        response.setUserId(USER_ID);
        return response;
    }
}
