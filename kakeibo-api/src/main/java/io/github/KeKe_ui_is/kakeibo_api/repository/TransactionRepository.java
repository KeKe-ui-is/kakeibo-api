package io.github.KeKe_ui_is.kakeibo_api.repository;


import io.github.KeKe_ui_is.kakeibo_api.dto.response.TransactionResponse;
import io.github.KeKe_ui_is.kakeibo_api.model.Transaction;
import io.github.KeKe_ui_is.kakeibo_api.model.TransactionType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TransactionRepository {
    /**
     * userIdに紐づく全権検索
     * @param userId ユーザーID
     * @return TransactionResponse 収支をカテゴリーIDと紐づけたもの
     */
    List<TransactionResponse> findAllByUserId(@Param("userId") Long userId);

    TransactionResponse findByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    /**
     * カテゴリIDとユーザIDを紐づけて検索
     * @param categoryId 収支ID
     * @param userId　ユーザーID
     * @return　
     */
    TransactionType findCategoryTypeByIdAndUserId(
            @Param("categoryId") Long categoryId,
            @Param("userId") Long userId
    );

    /**
     * 追加
     * @param transaction 収支
     * @return int
     */
    int insert(Transaction transaction);

    /**
     * 更新
     * @param transaction 収支
     * @return
     */
    int update(Transaction transaction);

    /**
     * 削除
     * @param id Transaction id
     * @param userId User id
     * @return 削除件数
     */
    int deleteByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    /**
     * 指定されたユーザーの収支を期間で検索します。
     *
     * @param userId    ユーザーID
     * @param startDate 検索開始日（この日を含む）
     * @param endDate   検索終了日（この日を含まない）
     * @return 条件に一致する収支一覧。該当データがない場合は空の一覧
     */
    List<TransactionResponse> findByUserIdAndPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
