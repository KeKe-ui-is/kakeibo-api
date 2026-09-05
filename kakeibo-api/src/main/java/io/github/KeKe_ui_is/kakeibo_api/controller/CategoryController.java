package io.github.KeKe_ui_is.kakeibo_api.controller;

import io.github.KeKe_ui_is.kakeibo_api.dto.request.CategoryCreateRequest;
import io.github.KeKe_ui_is.kakeibo_api.dto.request.CategoryUpdateRequest;
import io.github.KeKe_ui_is.kakeibo_api.dto.response.CategoryResponse;
import io.github.KeKe_ui_is.kakeibo_api.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * カテゴリに関するAPIを提供するControllerです。
 *
 * カテゴリの取得、登録、更新、有効化、無効化および削除を行います。
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    /**
     * 認証機能を実装するまで使用する開発用ユーザーIDです。
     */
    private static final Long DEVELOPMENT_USER_ID = 1L;

    private final CategoryService categoryService;

    /**
     * CategoryControllerを生成します。
     *
     * @param categoryService カテゴリに関する処理を行うService
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 開発用ユーザーに紐づくすべてのカテゴリを取得します。
     * 無効化されているカテゴリも取得対象に含みます。
     *
     * @return カテゴリの一覧
     */
    @GetMapping("/findAll")
    public List<CategoryResponse> findAll() {
        return categoryService.findAll(DEVELOPMENT_USER_ID);
    }

    /**
     * 開発用ユーザーに紐づく有効なカテゴリを取得します。
     * is_activeがTRUEのカテゴリだけを取得します。
     *
     * @return 有効なカテゴリの一覧
     */
    @GetMapping("/findActive")
    public List<CategoryResponse> findActive() {
        return categoryService.findActive(DEVELOPMENT_USER_ID);
    }

    /**
     * 指定されたカテゴリIDに該当するカテゴリを1件取得します。
     *
     * @param categoryId 取得するカテゴリID
     * @return 指定されたカテゴリ
     */
    @GetMapping("/find/{categoryId}")
    public CategoryResponse findById(
            @PathVariable("categoryId") Long categoryId) {

        return categoryService.findById(
                DEVELOPMENT_USER_ID,
                categoryId
        );
    }

    /**
     * 新しいカテゴリを登録します。
     *
     * リクエストが正常な場合は、HTTPステータス201 Createdと
     * 登録されたカテゴリを返します。
     *
     * @param request カテゴリの登録内容
     * @return 登録されたカテゴリ
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse registerCategory(
            @Valid @RequestBody CategoryCreateRequest request) {

        return categoryService.create(
                DEVELOPMENT_USER_ID,
                request
        );
    }

    /**
     * 指定されたカテゴリの名前と表示順を更新します。
     *
     * 収入・支出区分と有効・無効状態は、このAPIでは変更しません。
     *
     * @param categoryId 更新するカテゴリID
     * @param request    カテゴリの更新内容
     * @return 更新後のカテゴリ
     */
    @PutMapping("/update/{categoryId}")
    public CategoryResponse updateCategory(
            @PathVariable("categoryId") Long categoryId,
            @Valid @RequestBody CategoryUpdateRequest request) {

        return categoryService.update(
                DEVELOPMENT_USER_ID,
                categoryId,
                request
        );
    }

    /**
     * 指定されたカテゴリを有効化します。
     *
     * カテゴリのis_activeをTRUEに更新します。
     *
     * @param categoryId 有効化するカテゴリID
     * @return 有効化後のカテゴリ
     */
    @PatchMapping("/activate/{categoryId}")
    public CategoryResponse activateCategory(
            @PathVariable("categoryId") Long categoryId) {

        return categoryService.activate(
                DEVELOPMENT_USER_ID,
                categoryId
        );
    }

    /**
     * 指定されたカテゴリを無効化します。
     *
     * カテゴリ自体は削除せず、is_activeをFALSEに更新します。
     *
     * @param categoryId 無効化するカテゴリID
     * @return 無効化後のカテゴリ
     */
    @PatchMapping("/deactivate/{categoryId}")
    public CategoryResponse deactivateCategory(
            @PathVariable("categoryId") Long categoryId) {

        return categoryService.deactivate(
                DEVELOPMENT_USER_ID,
                categoryId
        );
    }

    /**
     * 指定されたカテゴリを物理削除します。
     *
     * 収支データから使用されているカテゴリは削除できません。
     * 削除に成功した場合はHTTPステータス204 No Contentを返します。
     *
     * @param categoryId 削除するカテゴリID
     */
    @DeleteMapping("/delete/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @PathVariable("categoryId") Long categoryId) {

        categoryService.delete(
                DEVELOPMENT_USER_ID,
                categoryId
        );
    }
}