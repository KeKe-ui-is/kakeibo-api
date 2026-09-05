package io.github.KeKe_ui_is.kakeibo_api.exception;

/**
 * 指定されたデータが存在しない場合に使用する例外です。
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}