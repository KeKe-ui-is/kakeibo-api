package io.github.KeKe_ui_is.kakeibo_api.controller;

import io.github.KeKe_ui_is.kakeibo_api.dto.response.TransactionResponse;
import io.github.KeKe_ui_is.kakeibo_api.exception.GlobalExceptionHandler;
import io.github.KeKe_ui_is.kakeibo_api.model.ExpenseType;
import io.github.KeKe_ui_is.kakeibo_api.model.TransactionType;
import io.github.KeKe_ui_is.kakeibo_api.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionController テスト")
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TransactionController controller = new TransactionController(transactionService);
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("正しい年月を指定すると対象月の収支一覧と200を返す")
    void shouldReturnTransactionsAndOkWhenYearMonthIsValid() throws Exception {
        TransactionResponse response = createResponse();
        when(transactionService.findByMonth(1L, YearMonth.of(2026, 9)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/transactions/findByMonth")
                        .param("yearMonth", "2026-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].categoryName").value("食費"))
                .andExpect(jsonPath("$[0].amount").value(1200))
                .andExpect(jsonPath("$[0].transactionDate").value("2026-09-06"));

        verify(transactionService).findByMonth(1L, YearMonth.of(2026, 9));
    }

    @Test
    @DisplayName("yearMonthを指定しない場合は400を返す")
    void shouldReturnBadRequestWhenYearMonthIsMissing() throws Exception {
        mockMvc.perform(get("/api/transactions/findByMonth"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "必須パラメータが不足しているか、形式が正しくありません。"
                                + "yearMonthは2026-09の形式で指定してください"
                ))
                .andExpect(jsonPath("$.path").value("/api/transactions/findByMonth"));
    }

    @Test
    @DisplayName("yearMonthの形式が不正な場合は400を返す")
    void shouldReturnBadRequestWhenYearMonthFormatIsInvalid() throws Exception {
        mockMvc.perform(get("/api/transactions/findByMonth")
                        .param("yearMonth", "2026/09"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "必須パラメータが不足しているか、形式が正しくありません。"
                                + "yearMonthは2026-09の形式で指定してください"
                ));
    }

    @Test
    @DisplayName("存在しない月を指定した場合は400を返す")
    void shouldReturnBadRequestWhenMonthIsInvalid() throws Exception {
        mockMvc.perform(get("/api/transactions/findByMonth")
                        .param("yearMonth", "2026-13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("正常な収支登録リクエストの場合は201を返す")
    void shouldReturnCreatedWhenRegisterRequestIsValid() throws Exception {
        TransactionResponse response = createResponse();
        when(transactionService.create(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        String json = """
                {
                  "categoryId": 2,
                  "amount": 1200,
                  "transactionDate": "2026-09-06",
                  "expenseType": "VARIABLE",
                  "memo": "昼食"
                }
                """;

        mockMvc.perform(post("/api/transactions/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("必須項目が不足した収支登録リクエストの場合は400を返す")
    void shouldReturnBadRequestWhenRegisterRequestIsInvalid() throws Exception {
        String json = """
                {
                  "categoryId": 2,
                  "transactionDate": "2026-09-06",
                  "expenseType": "VARIABLE"
                }
                """;

        mockMvc.perform(post("/api/transactions/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("入力内容に誤りがあります"))
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    @DisplayName("不正な日付形式の収支登録リクエストの場合は400を返す")
    void shouldReturnBadRequestWhenTransactionDateIsInvalid() throws Exception {
        String json = """
                {
                  "categoryId": 2,
                  "amount": 1200,
                  "transactionDate": "2026/09/06",
                  "expenseType": "VARIABLE"
                }
                """;

        mockMvc.perform(post("/api/transactions/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("JSONの形式または入力値が正しくありません"));
    }

    private TransactionResponse createResponse() {
        TransactionResponse response = new TransactionResponse();
        response.setId(10L);
        response.setUserId(1L);
        response.setCategoryId(2L);
        response.setCategoryName("食費");
        response.setTransactionType(TransactionType.EXPENSE);
        response.setAmount(1_200L);
        response.setTransactionDate(LocalDate.of(2026, 9, 6));
        response.setExpenseType(ExpenseType.VARIABLE);
        response.setMemo("昼食");
        return response;
    }
}
