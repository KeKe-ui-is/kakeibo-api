package io.github.KeKe_ui_is.kakeibo_api.exception;

/**
 * 業務ルールに反する入力が行われた場合に使用する例外です。
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}