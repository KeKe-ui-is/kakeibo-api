package io.github.KeKe_ui_is.kakeibo_api.validator;

import io.github.KeKe_ui_is.kakeibo_api.exception.ConflictException;
import io.github.KeKe_ui_is.kakeibo_api.model.TransactionType;
import io.github.KeKe_ui_is.kakeibo_api.repository.CategoryRepository;
import org.springframework.stereotype.Component;

/**
 * カテゴリに関する業務入力チェックを行います。
 */
@Component
public class CategoryValidator {

    private final CategoryRepository categoryRepository;

    public CategoryValidator(
            CategoryRepository categoryRepository) {

        this.categoryRepository = categoryRepository;
    }

    /**
     * 同じ名前と収支区分のカテゴリが存在しないことを確認します。
     *
     * @param userId             ユーザーID
     * @param name               カテゴリ名
     * @param transactionType    収入・支出区分
     * @param excludedCategoryId 更新時に除外するカテゴリID。登録時はnull
     */
    public void validateDuplicate(
            Long userId,
            String name,
            TransactionType transactionType,
            Long excludedCategoryId) {

        int count;

        if (excludedCategoryId == null) {
            count = categoryRepository.countByNameAndType(
                    userId,
                    name,
                    transactionType
            );
        } else {
            count =
                    categoryRepository
                            .countByNameAndTypeExcludingId(
                                    userId,
                                    name,
                                    transactionType,
                                    excludedCategoryId
                            );
        }

        if (count > 0) {
            throw new ConflictException(
                    "同じ名前と収支区分のカテゴリがすでに存在します"
            );
        }
    }

    /**
     * カテゴリが収支データで使用されていないことを確認します。
     *
     * @param userId     ユーザーID
     * @param categoryId カテゴリID
     */
    public void validateDeletable(
            Long userId,
            Long categoryId) {

        int transactionCount =
                categoryRepository.countTransactions(
                        categoryId,
                        userId
                );

        if (transactionCount > 0) {
            throw new ConflictException(
                    "収支で使用されているカテゴリは削除できません。"
                            + "無効化してください"
            );
        }
    }
}