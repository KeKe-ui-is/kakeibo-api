package io.github.KeKe_ui_is.kakeibo_api.controller;

import io.github.KeKe_ui_is.kakeibo_api.dto.response.CategoryResponse;
import io.github.KeKe_ui_is.kakeibo_api.exception.ConflictException;
import io.github.KeKe_ui_is.kakeibo_api.exception.GlobalExceptionHandler;
import io.github.KeKe_ui_is.kakeibo_api.exception.ResourceNotFoundException;
import io.github.KeKe_ui_is.kakeibo_api.model.TransactionType;
import io.github.KeKe_ui_is.kakeibo_api.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController テスト")
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CategoryController controller = new CategoryController(categoryService);
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("全カテゴリ取得ではカテゴリ一覧と200を返す")
    void shouldReturnAllCategoriesAndOk() throws Exception {
        when(categoryService.findAll(1L)).thenReturn(List.of(createResponse()));

        mockMvc.perform(get("/api/categories/findAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("食費"));

        verify(categoryService).findAll(1L);
    }

    @Test
    @DisplayName("正常なカテゴリ登録リクエストの場合は201を返す")
    void shouldReturnCreatedWhenRegisterRequestIsValid() throws Exception {
        when(categoryService.create(eq(1L), any())).thenReturn(createResponse());

        String json = """
                {
                  "name": "食費",
                  "transactionType": "EXPENSE",
                  "displayOrder": 1
                }
                """;

        mockMvc.perform(post("/api/categories/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("食費"));
    }

    @Test
    @DisplayName("カテゴリ名が空の場合は400とフィールドエラーを返す")
    void shouldReturnBadRequestWhenCategoryNameIsBlank() throws Exception {
        String json = """
                {
                  "name": "",
                  "transactionType": "EXPENSE",
                  "displayOrder": 1
                }
                """;

        mockMvc.perform(post("/api/categories/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("入力内容に誤りがあります"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    @DisplayName("カテゴリが存在しない場合は404の共通エラーレスポンスを返す")
    void shouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
        when(categoryService.findById(1L, 99L))
                .thenThrow(new ResourceNotFoundException("カテゴリが見つかりません"));

        mockMvc.perform(get("/api/categories/find/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("カテゴリが見つかりません"))
                .andExpect(jsonPath("$.path").value("/api/categories/find/99"));
    }

    @Test
    @DisplayName("使用中カテゴリを削除しようとした場合は409を返す")
    void shouldReturnConflictWhenDeletingUsedCategory() throws Exception {
        org.mockito.Mockito.doThrow(new ConflictException("収支で使用されているカテゴリは削除できません。無効化してください"))
                .when(categoryService).delete(1L, 10L);

        mockMvc.perform(delete("/api/categories/delete/10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("収支で使用されているカテゴリは削除できません。無効化してください"));
    }

    private CategoryResponse createResponse() {
        CategoryResponse response = new CategoryResponse();
        response.setId(10L);
        response.setUserId(1L);
        response.setName("食費");
        response.setTransactionType(TransactionType.EXPENSE);
        response.setDisplayOrder(1);
        response.setActive(true);
        return response;
    }
}
