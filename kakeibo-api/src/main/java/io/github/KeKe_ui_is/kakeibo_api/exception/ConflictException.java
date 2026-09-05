package io.github.KeKe_ui_is.kakeibo_api.exception;

/**
 * 既存データとの重複や競合が発生した場合に使用する例外です。
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}