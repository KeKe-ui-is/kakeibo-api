package io.github.KeKe_ui_is.kakeibo_api.service;

import io.github.KeKe_ui_is.kakeibo_api.dto.request.TransactionRequest;
import io.github.KeKe_ui_is.kakeibo_api.dto.response.TransactionResponse;
import io.github.KeKe_ui_is.kakeibo_api.model.ExpenseType;
import io.github.KeKe_ui_is.kakeibo_api.model.Transaction;
import io.github.KeKe_ui_is.kakeibo_api.model.TransactionType;
import io.github.KeKe_ui_is.kakeibo_api.repository.CategoryRepository;
import io.github.KeKe_ui_is.kakeibo_api.repository.TransactionRepository;
import io.github.KeKe_ui_is.kakeibo_api.validator.TransactionValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionValidator transactionValidator;

    public TransactionService(TransactionRepository transactionRepository, TransactionValidator transactionValidator) {
        this.transactionRepository = transactionRepository;
        this.transactionValidator = transactionValidator;
    }

    /**
     * ユーザIDに紐づく全件検索
     * @param userId ユーザID
     * @return TransactionResponse
     */
    public List<TransactionResponse> findAll(Long userId) {
        return transactionRepository.findAllByUserId(userId);
    }

    /**
     * 単体検索
     * @param userId ユーザID
     * @param id TransactionID
     * @return TransactionResponse
     */
    public TransactionResponse findById(Long userId, Long id) {
        TransactionResponse transaction =
                transactionRepository.findByIdAndUserId(id, userId);
//        見つからなかったとき
        if (transaction == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "収支が見つかりません"
            );
        }

        return transaction;
    }

    /**
     * 指定されたユーザーの1か月分の収支明細を取得します。
     *
     * @param userId    ユーザーID
     * @param yearMonth 検索対象の年月
     * @return 対象月の収支一覧。該当データがない場合は空の一覧
     */
    public List<TransactionResponse> findByMonth(Long userId, YearMonth yearMonth) {

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.plusMonths(1).atDay(1);

        return transactionRepository.findByUserIdAndPeriod(userId, startDate, endDate
        );
    }

    /**
     * 新規作成
     * @param userId
     * @param request
     * @return TransactionResponse
     */
    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        transactionValidator.validateCategoryAndExpenseType(userId, request);

        Transaction transaction = toTransaction(null, userId, request);

        transactionRepository.insert(transaction);

        return findById(userId, transaction.getId());
    }

    @Transactional
    public TransactionResponse update(Long userId, Long id, TransactionRequest request) {
        // 対象が存在し、本人のデータであることを確認
        findById(userId, id);
        transactionValidator.validateCategoryAndExpenseType(userId, request);

        Transaction transaction = toTransaction(id, userId, request);

        transactionRepository.update(transaction);

        return findById(userId, id);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        int deletedCount = transactionRepository.deleteByIdAndUserId(id, userId);

        if (deletedCount == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "収支が見つかりません"
            );
        }
    }

    /**
     * TransactionResponseをTransactionに組み立てる
     * @param id TransactionID　SQLで自動採番される
     * @param userId ユーザID
     * @param request TransactionResponse
     * @return Transaction
     */
    private Transaction toTransaction(Long id, Long userId, TransactionRequest request) {
        Transaction transaction = new Transaction();

        transaction.setId(id);
        transaction.setUserId(userId);
        transaction.setCategoryId(request.getCategoryId());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setExpenseType(request.getExpenseType());
        transaction.setMemo(request.getMemo());

        return transaction;
    }


}