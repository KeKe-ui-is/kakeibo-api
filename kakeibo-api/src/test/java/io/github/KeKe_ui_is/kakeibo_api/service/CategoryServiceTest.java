package io.github.KeKe_ui_is.kakeibo_api.service;

import io.github.KeKe_ui_is.kakeibo_api.dto.request.CategoryCreateRequest;
import io.github.KeKe_ui_is.kakeibo_api.dto.request.CategoryUpdateRequest;
import io.github.KeKe_ui_is.kakeibo_api.dto.response.CategoryResponse;
import io.github.KeKe_ui_is.kakeibo_api.exception.ResourceNotFoundException;
import io.github.KeKe_ui_is.kakeibo_api.model.Category;
import io.github.KeKe_ui_is.kakeibo_api.model.TransactionType;
import io.github.KeKe_ui_is.kakeibo_api.repository.CategoryRepository;
import io.github.KeKe_ui_is.kakeibo_api.validator.CategoryValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
@DisplayName("CategoryService テスト")
class CategoryServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryValidator categoryValidator;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("全カテゴリ取得ではユーザーIDに紐づくカテゴリ一覧を返す")
    void shouldReturnAllCategoriesForUser() {
        List<CategoryResponse> expected = List.of(createResponse(1L, TransactionType.INCOME));
        when(categoryRepository.findAllByUserId(USER_ID)).thenReturn(expected);

        List<CategoryResponse> actual = categoryService.findAll(USER_ID);

        assertSame(expected, actual);
        verify(categoryRepository).findAllByUserId(USER_ID);
    }

    @Test
    @DisplayName("有効カテゴリ取得ではユーザーIDに紐づく有効カテゴリ一覧を返す")
    void shouldReturnActiveCategoriesForUser() {
        List<CategoryResponse> expected = List.of(createResponse(2L, TransactionType.EXPENSE));
        when(categoryRepository.findActiveByUserId(USER_ID)).thenReturn(expected);

        List<CategoryResponse> actual = categoryService.findActive(USER_ID);

        assertSame(expected, actual);
        verify(categoryRepository).findActiveByUserId(USER_ID);
    }

    @Test
    @DisplayName("IDに一致するカテゴリが存在する場合はそのカテゴリを返す")
    void shouldReturnCategoryWhenCategoryExists() {
        CategoryResponse expected = createResponse(10L, TransactionType.EXPENSE);
        when(categoryRepository.findByIdAndUserId(10L, USER_ID)).thenReturn(expected);

        CategoryResponse actual = categoryService.findById(USER_ID, 10L);

        assertSame(expected, actual);
    }

    @Test
    @DisplayName("IDに一致するカテゴリが存在しない場合はResourceNotFoundExceptionを投げる")
    void shouldThrowNotFoundWhenCategoryDoesNotExist() {
        when(categoryRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.findById(USER_ID, 99L)
        );

        assertEquals("カテゴリが見つかりません", exception.getMessage());
    }

    @Test
    @DisplayName("カテゴリ登録時は名前の前後空白を除去して保存し登録後のカテゴリを返す")
    void shouldTrimNameCreateCategoryAndReturnCreatedCategory() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("  食費  ");
        request.setTransactionType(TransactionType.EXPENSE);
        request.setDisplayOrder(3);

        CategoryResponse expected = createResponse(100L, TransactionType.EXPENSE);
        expected.setName("食費");

        doAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(100L);
            return 1;
        }).when(categoryRepository).insert(any(Category.class));
        when(categoryRepository.findByIdAndUserId(100L, USER_ID)).thenReturn(expected);

        CategoryResponse actual = categoryService.create(USER_ID, request);

        assertSame(expected, actual);
        verify(categoryValidator).validateDuplicate(USER_ID, "食費", TransactionType.EXPENSE, null);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).insert(captor.capture());
        Category saved = captor.getValue();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals("食費", saved.getName());
        assertEquals(TransactionType.EXPENSE, saved.getTransactionType());
        assertEquals(3, saved.getDisplayOrder());
    }

    @Test
    @DisplayName("カテゴリ更新時は既存の収支区分を使って重複確認し名前と表示順を更新する")
    void shouldUpdateCategoryUsingExistingTransactionType() {
        Long categoryId = 10L;
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("  給料  ");
        request.setDisplayOrder(1);

        CategoryResponse existing = createResponse(categoryId, TransactionType.INCOME);
        existing.setName("給与");
        CategoryResponse updated = createResponse(categoryId, TransactionType.INCOME);
        updated.setName("給料");

        when(categoryRepository.findByIdAndUserId(categoryId, USER_ID))
                .thenReturn(existing, updated);

        CategoryResponse actual = categoryService.update(USER_ID, categoryId, request);

        assertSame(updated, actual);
        verify(categoryValidator).validateDuplicate(USER_ID, "給料", TransactionType.INCOME, categoryId);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).update(captor.capture());
        Category saved = captor.getValue();
        assertEquals(categoryId, saved.getId());
        assertEquals(USER_ID, saved.getUserId());
        assertEquals("給料", saved.getName());
        assertEquals(1, saved.getDisplayOrder());
    }

    @Test
    @DisplayName("存在しないカテゴリを更新する場合は重複確認や更新処理を行わない")
    void shouldNotUpdateWhenCategoryDoesNotExist() {
        when(categoryRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.update(USER_ID, 99L, new CategoryUpdateRequest())
        );

        verify(categoryValidator, never()).validateDuplicate(any(), any(), any(), any());
        verify(categoryRepository, never()).update(any(Category.class));
    }

    @Test
    @DisplayName("カテゴリ有効化では存在確認後にactiveをtrueへ更新して結果を返す")
    void shouldActivateCategoryAndReturnUpdatedCategory() {
        Long categoryId = 10L;
        CategoryResponse before = createResponse(categoryId, TransactionType.EXPENSE);
        before.setActive(false);
        CategoryResponse after = createResponse(categoryId, TransactionType.EXPENSE);
        after.setActive(true);
        when(categoryRepository.findByIdAndUserId(categoryId, USER_ID)).thenReturn(before, after);

        CategoryResponse actual = categoryService.activate(USER_ID, categoryId);

        assertSame(after, actual);
        verify(categoryRepository).updateActive(categoryId, USER_ID, true);
    }

    @Test
    @DisplayName("カテゴリ無効化では存在確認後にactiveをfalseへ更新して結果を返す")
    void shouldDeactivateCategoryAndReturnUpdatedCategory() {
        Long categoryId = 10L;
        CategoryResponse before = createResponse(categoryId, TransactionType.EXPENSE);
        before.setActive(true);
        CategoryResponse after = createResponse(categoryId, TransactionType.EXPENSE);
        after.setActive(false);
        when(categoryRepository.findByIdAndUserId(categoryId, USER_ID)).thenReturn(before, after);

        CategoryResponse actual = categoryService.deactivate(USER_ID, categoryId);

        assertSame(after, actual);
        verify(categoryRepository).updateActive(categoryId, USER_ID, false);
    }

    @Test
    @DisplayName("カテゴリ削除では存在確認と削除可否確認後に削除する")
    void shouldValidateAndDeleteCategory() {
        Long categoryId = 10L;
        when(categoryRepository.findByIdAndUserId(categoryId, USER_ID))
                .thenReturn(createResponse(categoryId, TransactionType.EXPENSE));

        categoryService.delete(USER_ID, categoryId);

        verify(categoryValidator).validateDeletable(USER_ID, categoryId);
        verify(categoryRepository).deleteByIdAndUserId(categoryId, USER_ID);
    }

    private CategoryResponse createResponse(Long id, TransactionType type) {
        CategoryResponse response = new CategoryResponse();
        response.setId(id);
        response.setUserId(USER_ID);
        response.setName("テストカテゴリ");
        response.setTransactionType(type);
        response.setDisplayOrder(1);
        response.setActive(true);
        return response;
    }
}
