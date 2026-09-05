package io.github.KeKe_ui_is.kakeibo_api.controller;

import io.github.KeKe_ui_is.kakeibo_api.dto.request.TransactionRequest;
import io.github.KeKe_ui_is.kakeibo_api.dto.response.TransactionResponse;
import io.github.KeKe_ui_is.kakeibo_api.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Long DEVELOPMENT_USER_ID = 1L;

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("findAll")
    public List<TransactionResponse> findAll() {
        return transactionService.findAll(DEVELOPMENT_USER_ID);
    }

    @GetMapping("find/{transactionId}")
    public TransactionResponse findById(@PathVariable Long transactionId) {
        return transactionService.findById(DEVELOPMENT_USER_ID, transactionId);
    }

    /**
     * 指定された年月の収支明細を取得します。
     *
     * @param yearMonth 検索対象の年月（例：2026-09）
     * @return 対象月の収支一覧
     */
    @GetMapping("/findByMonth")
    public List<TransactionResponse> findByMonth(@RequestParam("yearMonth") @DateTimeFormat(pattern = "uuuu-MM") YearMonth yearMonth) {

        return transactionService.findByMonth(
                DEVELOPMENT_USER_ID,
                yearMonth
        );
    }

    @PostMapping("register")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse registerTransaction(@Valid @RequestBody TransactionRequest request) {
        return transactionService.create(DEVELOPMENT_USER_ID, request
        );
    }

    @PutMapping("update/{transactionId}")
    public TransactionResponse updateTransaction(@PathVariable Long transactionId, @Valid @RequestBody TransactionRequest request) {
        return transactionService.update(DEVELOPMENT_USER_ID, transactionId, request);
    }

    @DeleteMapping("delete/{transactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable Long transactionId) {
        transactionService.delete(
                DEVELOPMENT_USER_ID,
                transactionId
        );
    }
}