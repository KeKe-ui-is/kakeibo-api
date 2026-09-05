package io.github.KeKe_ui_is.kakeibo_api.repository;

import io.github.KeKe_ui_is.kakeibo_api.dto.response.CategoryResponse;
import io.github.KeKe_ui_is.kakeibo_api.model.Category;
import io.github.KeKe_ui_is.kakeibo_api.model.TransactionType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * カテゴリテーブルを操作するRepositoryです。
 */
@Mapper
public interface CategoryRepository {

    /**
     * 指定されたユーザーに紐づくすべてのカテゴリを取得します。
     * 無効化されているカテゴリも取得対象に含みます。
     *
     * @param userId ユーザーID
     * @return ユーザーに紐づくカテゴリの一覧
     */
    List<CategoryResponse> findAllByUserId(
            @Param("userId") Long userId
    );

    /**
     * 指定されたユーザーに紐づく有効なカテゴリを取得します。
     * is_activeがTRUEのカテゴリだけを取得します。
     *
     * @param userId ユーザーID
     * @return 有効なカテゴリの一覧
     */
    List<CategoryResponse> findActiveByUserId(
            @Param("userId") Long userId
    );

    /**
     * カテゴリIDとユーザーIDを使用してカテゴリを1件取得します。
     * ユーザーIDも検索条件に含めることで、
     * 別のユーザーが所有するカテゴリの取得を防止します。
     *
     * @param categoryId カテゴリID
     * @param userId     ユーザーID
     * @return 条件に一致するカテゴリ。存在しない場合はnull
     */
    CategoryResponse findByIdAndUserId(
            @Param("categoryId") Long categoryId,
            @Param("userId") Long userId
    );

    /**
     * 指定されたユーザーに、同じカテゴリ名と収支区分を持つ
     * カテゴリが存在するか確認します。
     * 主にカテゴリ登録時の重複チェックに使用します。
     *
     * @param userId          ユーザーID
     * @param name            カテゴリ名
     * @param transactionType 収入・支出区分
     * @return 条件に一致するカテゴリの件数
     */
    int countByNameAndType(
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("transactionType") TransactionType transactionType
    );

    /**
     * 指定されたカテゴリIDを除外して、同じカテゴリ名と収支区分を持つ
     * カテゴリが存在するか確認します。
     * 主にカテゴリ更新時の重複チェックに使用します。
     *
     * @param userId          ユーザーID
     * @param name            カテゴリ名
     * @param transactionType 収入・支出区分
     * @param categoryId      重複チェックから除外するカテゴリID
     * @return 条件に一致するカテゴリの件数
     */
    int countByNameAndTypeExcludingId(
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("transactionType") TransactionType transactionType,
            @Param("categoryId") Long categoryId
    );

    /**
     * カテゴリを新規登録します。
     * 登録成功後、DBで自動生成されたカテゴリIDが
     * 引数のCategoryオブジェクトのidに設定されます。
     *
     * @param category 登録するカテゴリ
     * @return 登録された行数
     */
    int insert(Category category);

    /**
     * カテゴリ名と表示順を更新します。
     * カテゴリIDとユーザーIDが一致するカテゴリだけを更新します。
     *
     * @param category 更新内容を保持するカテゴリ
     * @return 更新された行数
     */
    int update(Category category);

    /**
     * 指定されたカテゴリの有効・無効状態を更新します。
     *
     * @param categoryId カテゴリID
     * @param userId     ユーザーID
     * @param active     trueの場合は有効、falseの場合は無効
     * @return 更新された行数
     */
    int updateActive(
            @Param("categoryId") Long categoryId,
            @Param("userId") Long userId,
            @Param("active") boolean active
    );

    /**
     * 指定されたカテゴリを使用している収支データの件数を取得します。
     * カテゴリを物理削除できるか確認するために使用します。
     *
     * @param categoryId カテゴリID
     * @param userId     ユーザーID
     * @return カテゴリを使用している収支データの件数
     */
    int countTransactions(
            @Param("categoryId") Long categoryId,
            @Param("userId") Long userId
    );

    /**
     * カテゴリIDとユーザーIDを使用してカテゴリを物理削除します。
     * 収支データから使用されている場合は、Serviceで削除を禁止します。
     *
     * @param categoryId 削除するカテゴリID
     * @param userId     ユーザーID
     * @return 削除された行数
     */
    int deleteByIdAndUserId(
            @Param("categoryId") Long categoryId,
            @Param("userId") Long userId
    );
}