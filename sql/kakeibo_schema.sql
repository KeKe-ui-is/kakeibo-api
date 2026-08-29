-- 家計簿API 初期DB定義
-- 対象: MySQL 8.0以降

CREATE DATABASE IF NOT EXISTS kakeibo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE kakeibo;

-- ユーザー
-- 認証機能を実装するまでは、末尾の開発用ユーザーを使用できます。
CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ユーザーID',
    email         VARCHAR(255) NOT NULL COMMENT 'メールアドレス',
    password_hash VARCHAR(255) NULL COMMENT 'ハッシュ化したパスワード（認証実装時に使用）',
    display_name  VARCHAR(100) NOT NULL COMMENT '表示名',
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '作成日時',
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新日時',

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE = InnoDB;

-- カテゴリ
-- 収入・支出の区分はカテゴリ側で管理し、収支テーブルとの重複を避けます。
CREATE TABLE categories (
    id               BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'カテゴリID',
    user_id          BIGINT      NOT NULL COMMENT 'ユーザーID',
    name             VARCHAR(50) NOT NULL COMMENT 'カテゴリ名',
    transaction_type ENUM('INCOME', 'EXPENSE') NOT NULL COMMENT '収入・支出区分',
    display_order    INT         NOT NULL DEFAULT 0 COMMENT '表示順',
    is_active        BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '使用可能か',
    created_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '作成日時',
    updated_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                 ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新日時',

    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uq_categories_id_user UNIQUE (id, user_id),
    CONSTRAINT uq_categories_user_name_type
        UNIQUE (user_id, name, transaction_type),
    CONSTRAINT fk_categories_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB;

-- 収支
-- 収入と支出を1テーブルで管理します。
-- 支出の場合だけexpense_typeにFIXEDまたはVARIABLEを設定します。
CREATE TABLE transactions (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '収支ID',
    user_id          BIGINT       NOT NULL COMMENT 'ユーザーID',
    category_id      BIGINT       NOT NULL COMMENT 'カテゴリID',
    amount           BIGINT       NOT NULL COMMENT '金額（円）',
    transaction_date DATE         NOT NULL COMMENT '収支日',
    expense_type     ENUM('FIXED', 'VARIABLE') NULL COMMENT '固定費・変動費区分（支出のみ）',
    memo             VARCHAR(500) NULL COMMENT 'メモ',
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '作成日時',
    updated_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                  ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新日時',

    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT ck_transactions_amount_positive CHECK (amount > 0),
    CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_transactions_category_user
        FOREIGN KEY (category_id, user_id) REFERENCES categories (id, user_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    INDEX idx_transactions_user_date (user_id, transaction_date),
    INDEX idx_transactions_category (category_id)
) ENGINE = InnoDB;

-- 開発用の初期データ
-- 認証機能を実装するまでは、user_id = 1として動作確認できます。
INSERT INTO users (email, password_hash, display_name)
VALUES ('dev@example.com', NULL, '開発ユーザー');

INSERT INTO categories
    (user_id, name, transaction_type, display_order)
VALUES
    (1, '給与',   'INCOME',  10),
    (1, '臨時収入', 'INCOME',  20),
    (1, '食費',   'EXPENSE', 10),
    (1, '日用品',  'EXPENSE', 20),
    (1, '住居費',  'EXPENSE', 30),
    (1, '水道光熱費', 'EXPENSE', 40),
    (1, '通信費',  'EXPENSE', 50),
    (1, '交通費',  'EXPENSE', 60),
    (1, '娯楽費',  'EXPENSE', 70),
    (1, 'その他',  'EXPENSE', 80);
