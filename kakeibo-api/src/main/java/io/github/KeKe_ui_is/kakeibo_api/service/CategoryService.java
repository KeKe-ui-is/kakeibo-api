package io.github.KeKe_ui_is.kakeibo_api.service;

import io.github.KeKe_ui_is.kakeibo_api.dto.request.CategoryCreateRequest;
import io.github.KeKe_ui_is.kakeibo_api.dto.request.CategoryUpdateRequest;
import io.github.KeKe_ui_is.kakeibo_api.dto.response.CategoryResponse;
import io.github.KeKe_ui_is.kakeibo_api.exception.ResourceNotFoundException;
import io.github.KeKe_ui_is.kakeibo_api.model.Category;
import io.github.KeKe_ui_is.kakeibo_api.model.TransactionType;
import io.github.KeKe_ui_is.kakeibo_api.repository.CategoryRepository;
import io.github.KeKe_ui_is.kakeibo_api.validator.CategoryValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * カテゴリに関する業務処理を提供するServiceです。
 *
 * カテゴリの取得、登録、更新、有効化、無効化、
 * 削除および重複チェックを行います。
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryValidator categoryValidator;

    /**
     * CategoryServiceを生成します。
     *
     * @param categoryRepository カテゴリテーブルを操作するRepository
     */
    public CategoryService(CategoryRepository categoryRepository, CategoryValidator categoryValidator) {
        this.categoryRepository = categoryRepository;
        this.categoryValidator = categoryValidator;
    }

    /**
     * 指定されたユーザーに紐づくすべてのカテゴリを取得します。
     * 無効化されているカテゴリも取得対象に含みます。
     *
     * @param userId ユーザーID
     * @return ユーザーに紐づくカテゴリの一覧
     */
    public List<CategoryResponse> findAll(Long userId) {
        return categoryRepository.findAllByUserId(userId);
    }

    /**
     * 指定されたユーザーに紐づく有効なカテゴリを取得します。
     * is_activeがTRUEのカテゴリだけを取得します。
     *
     * @param userId ユーザーID
     * @return 有効なカテゴリの一覧
     */
    public List<CategoryResponse> findActive(Long userId) {
        return categoryRepository.findActiveByUserId(userId);
    }

    /**
     * カテゴリIDとユーザーIDを使用してカテゴリを1件取得します。
     *
     * @param userId     ユーザーID
     * @param categoryId カテゴリID
     * @return 条件に一致するカテゴリ
     * @throws ResponseStatusException カテゴリが存在しない場合
     */
    public CategoryResponse findById(Long userId, Long categoryId) {
        CategoryResponse category = categoryRepository.findByIdAndUserId(categoryId, userId);

        if (category == null) {
            throw new ResourceNotFoundException("カテゴリが見つかりません");
        }
        return category;
    }

    /**
     * 新しいカテゴリを登録します。
     *
     * カテゴリ名の前後にある空白を削除し、
     * 同じユーザー、カテゴリ名、収支区分のカテゴリが
     * 存在しないことを確認してから登録します。
     *
     * @param userId ユーザーID
     * @param request カテゴリの登録内容
     * @return 登録されたカテゴリ
     * @throws ResponseStatusException 同じ名前と収支区分のカテゴリが存在する場合
     */
    @Transactional
    public CategoryResponse create(Long userId, CategoryCreateRequest request) {
        String name = request.getName().strip();
        categoryValidator.validateDuplicate(userId, name, request.getTransactionType(), null);

        Category category = new Category();
        category.setUserId(userId);
        category.setName(name);
        category.setTransactionType(request.getTransactionType());
        category.setDisplayOrder(request.getDisplayOrder());

        categoryRepository.insert(category);

        return findById(userId, category.getId());
    }

    /**
     * 指定されたカテゴリの名前と表示順を更新します。
     *
     * 収入・支出区分は変更せず、登録されている区分を維持します。
     * また、更新対象以外に同じ名前と収支区分のカテゴリが
     * 存在しないことを確認します。
     *
     * @param userId     ユーザーID
     * @param categoryId 更新するカテゴリID
     * @param request    カテゴリの更新内容
     * @return 更新後のカテゴリ
     * @throws ResponseStatusException カテゴリが存在しない場合、
     *                                 または重複するカテゴリが存在する場合
     */
    @Transactional
    public CategoryResponse update(Long userId, Long categoryId, CategoryUpdateRequest request) {

        CategoryResponse existing = findById(userId, categoryId);

        String name = request.getName().strip();
        categoryValidator.validateDuplicate(userId, name, existing.getTransactionType(), categoryId);

        Category category = new Category();
        category.setId(categoryId);
        category.setUserId(userId);
        category.setName(name);
        category.setDisplayOrder(request.getDisplayOrder());

        categoryRepository.update(category);

        return findById(userId, categoryId);
    }

    /**
     * 指定されたカテゴリを有効化します。
     *
     * カテゴリのis_activeをTRUEに更新します。
     *
     * @param userId     ユーザーID
     * @param categoryId 有効化するカテゴリID
     * @return 有効化後のカテゴリ
     * @throws ResponseStatusException カテゴリが存在しない場合
     */
    @Transactional
    public CategoryResponse activate(Long userId, Long categoryId) {

        findById(userId, categoryId);

        categoryRepository.updateActive(categoryId, userId, true);

        return findById(userId, categoryId);
    }

    /**
     * 指定されたカテゴリを無効化します。
     *
     * カテゴリを物理削除せず、
     * is_activeをFALSEに更新します。
     *
     * @param userId     ユーザーID
     * @param categoryId 無効化するカテゴリID
     * @return 無効化後のカテゴリ
     * @throws ResponseStatusException カテゴリが存在しない場合
     */
    @Transactional
    public CategoryResponse deactivate(Long userId, Long categoryId) {

        findById(userId, categoryId);

        categoryRepository.updateActive(categoryId, userId, false);

        return findById(userId, categoryId);
    }

    /**
     * 指定されたカテゴリを物理削除します。
     *
     * 収支データから使用されているカテゴリは削除せず、
     * 無効化する必要があります。
     *
     * @param userId     ユーザーID
     * @param categoryId 削除するカテゴリID
     * @throws ResponseStatusException カテゴリが存在しない場合、
     *                                 または収支データから使用されている場合
     */
    @Transactional
    public void delete(Long userId, Long categoryId) {

        findById(userId, categoryId);

        categoryValidator.validateDeletable(userId, categoryId);

        categoryRepository.deleteByIdAndUserId(categoryId, userId);
    }

}