package io.github.KeKe_ui_is.kakeibo_api.validator;

import io.github.KeKe_ui_is.kakeibo_api.dto.request.TransactionRequest;
import io.github.KeKe_ui_is.kakeibo_api.exception.InvalidRequestException;
import io.github.KeKe_ui_is.kakeibo_api.model.TransactionType;
import io.github.KeKe_ui_is.kakeibo_api.repository.TransactionRepository;
import org.springframework.stereotype.Component;

/**
 * 収支に関する業務入力チェックを行います。
 */
@Component
public class TransactionValidator {

    private final TransactionRepository transactionRepository;

    public TransactionValidator(
            TransactionRepository transactionRepository) {

        this.transactionRepository = transactionRepository;
    }

    /**
     * カテゴリの収支区分と固定費・変動費区分の整合性を確認します。
     *
     * @param userId  ユーザーID
     * @param request 収支の入力内容
     */
    public void validateCategoryAndExpenseType(
            Long userId,
            TransactionRequest request) {

        TransactionType transactionType =
                transactionRepository
                        .findCategoryTypeByIdAndUserId(
                                request.getCategoryId(),
                                userId
                        );

        if (transactionType == null) {
            throw new InvalidRequestException(
                    "指定された有効なカテゴリが見つかりません"
            );
        }

        if (transactionType == TransactionType.INCOME
                && request.getExpenseType() != null) {

            throw new InvalidRequestException(
                    "収入カテゴリでは固定費・変動費区分を指定できません"
            );
        }

        if (transactionType == TransactionType.EXPENSE
                && request.getExpenseType() == null) {

            throw new InvalidRequestException(
                    "支出カテゴリでは固定費・変動費区分が必要です"
            );
        }
    }
}