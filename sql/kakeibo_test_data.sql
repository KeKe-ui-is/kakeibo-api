-- 家計簿API テストデータ
-- 対象期間: 2026年6月1日～2026年8月31日
-- 前提: kakeibo_schema.sqlを先に実行していること
-- 注意: このSQLを複数回実行すると、同じ収支が重複して登録されます。

USE kakeibo;

START TRANSACTION;

-- 開発用ユーザーと各カテゴリのIDを取得します。
SET @user_id := (
    SELECT id
    FROM users
    WHERE email = 'dev@example.com'
    LIMIT 1
);

SET @salary_category_id := (
    SELECT id FROM categories
    WHERE user_id = @user_id AND name = '給与' AND transaction_type = 'INCOME'
    LIMIT 1
);
SET @temporary_income_category_id := (
    SELECT id FROM categories
    WHERE user_id = @user_id AND name = '臨時収入' AND transaction_type = 'INCOME'
    LIMIT 1
);
SET @food_category_id := (
    SELECT id FROM categories
    WHERE user_id = @user_id AND name = '食費' AND transaction_type = 'EXPENSE'
    LIMIT 1
);
SET @daily_goods_category_id := (
    SELECT id FROM categories
    WHERE user_id = @user_id AND name = '日用品' AND transaction_type = 'EXPENSE'
    LIMIT 1
);
SET @housing_category_id := (
    SELECT id FROM categories
    WHERE user_id = @user_id AND name = '住居費' AND transaction_type = 'EXPENSE'
    LIMIT 1
);
SET @utility_category_id := (
    SELECT id FROM categories
    WHERE user_id = @user_id AND name = '水道光熱費' AND transaction_type = 'EXPENSE'
    LIMIT 1
);
SET @communication_category_id := (
    SELECT id FROM categories
    WHERE user_id = @user_id AND name = '通信費' AND transaction_type = 'EXPENSE'
    LIMIT 1
);
SET @transportation_category_id := (
    SELECT id FROM categories
    WHERE user_id = @user_id AND name = '交通費' AND transaction_type = 'EXPENSE'
    LIMIT 1
);
SET @entertainment_category_id := (
    SELECT id FROM categories
    WHERE user_id = @user_id AND name = '娯楽費' AND transaction_type = 'EXPENSE'
    LIMIT 1
);
SET @other_category_id := (
    SELECT id FROM categories
    WHERE user_id = @user_id AND name = 'その他' AND transaction_type = 'EXPENSE'
    LIMIT 1
);

INSERT INTO transactions
    (user_id, category_id, amount, transaction_date, expense_type, memo)
VALUES
    -- 2026年6月
    (@user_id, @housing_category_id,       76000, '2026-06-01', 'FIXED',    '6月分家賃'),
    (@user_id, @communication_category_id,  3980, '2026-06-03', 'FIXED',    'スマートフォン料金'),
    (@user_id, @communication_category_id,  4180, '2026-06-04', 'FIXED',    '自宅インターネット料金'),
    (@user_id, @food_category_id,           4520, '2026-06-05', 'VARIABLE', '食材まとめ買い'),
    (@user_id, @transportation_category_id, 5000, '2026-06-08', 'VARIABLE', '交通系ICカードへチャージ'),
    (@user_id, @utility_category_id,        5480, '2026-06-10', 'FIXED',    '電気料金'),
    (@user_id, @daily_goods_category_id,    2480, '2026-06-12', 'VARIABLE', '洗剤・ティッシュ'),
    (@user_id, @food_category_id,           3860, '2026-06-14', 'VARIABLE', '食材まとめ買い'),
    (@user_id, @entertainment_category_id,  3500, '2026-06-16', 'VARIABLE', '映画・飲み物'),
    (@user_id, @utility_category_id,        3920, '2026-06-18', 'FIXED',    'ガス・水道料金'),
    (@user_id, @food_category_id,           4750, '2026-06-20', 'VARIABLE', '食材まとめ買い'),
    (@user_id, @transportation_category_id, 2740, '2026-06-22', 'VARIABLE', '休日の電車代'),
    (@user_id, @other_category_id,          1820, '2026-06-24', 'VARIABLE', '薬・衛生用品'),
    (@user_id, @salary_category_id,       230000, '2026-06-25', NULL,       '6月分給与'),
    (@user_id, @food_category_id,           5180, '2026-06-26', 'VARIABLE', '食材とプロテイン'),
    (@user_id, @daily_goods_category_id,    2260, '2026-06-28', 'VARIABLE', 'キッチン用品'),
    (@user_id, @entertainment_category_id,  2180, '2026-06-30', 'VARIABLE', '書籍'),

    -- 2026年7月
    (@user_id, @housing_category_id,       76000, '2026-07-01', 'FIXED',    '7月分家賃'),
    (@user_id, @communication_category_id,  3980, '2026-07-03', 'FIXED',    'スマートフォン料金'),
    (@user_id, @communication_category_id,  4180, '2026-07-04', 'FIXED',    '自宅インターネット料金'),
    (@user_id, @food_category_id,           4680, '2026-07-05', 'VARIABLE', '食材まとめ買い'),
    (@user_id, @transportation_category_id, 5000, '2026-07-07', 'VARIABLE', '交通系ICカードへチャージ'),
    (@user_id, @utility_category_id,        6240, '2026-07-09', 'FIXED',    '電気料金'),
    (@user_id, @temporary_income_category_id,160000, '2026-07-10', NULL,    '夏季賞与'),
    (@user_id, @daily_goods_category_id,    3150, '2026-07-12', 'VARIABLE', '掃除用品・消耗品'),
    (@user_id, @food_category_id,           4210, '2026-07-14', 'VARIABLE', '食材まとめ買い'),
    (@user_id, @entertainment_category_id,  5200, '2026-07-16', 'VARIABLE', 'ゲーム購入'),
    (@user_id, @utility_category_id,        4010, '2026-07-18', 'FIXED',    'ガス・水道料金'),
    (@user_id, @food_category_id,           4990, '2026-07-20', 'VARIABLE', '食材とコーヒー'),
    (@user_id, @transportation_category_id, 3180, '2026-07-22', 'VARIABLE', '休日の電車代'),
    (@user_id, @other_category_id,          2980, '2026-07-24', 'VARIABLE', '散髪代'),
    (@user_id, @salary_category_id,       230000, '2026-07-25', NULL,       '7月分給与'),
    (@user_id, @food_category_id,           5360, '2026-07-27', 'VARIABLE', '食材まとめ買い'),
    (@user_id, @daily_goods_category_id,    1860, '2026-07-29', 'VARIABLE', '洗濯用品'),
    (@user_id, @entertainment_category_id,  3200, '2026-07-31', 'VARIABLE', '外出・カフェ'),

    -- 2026年8月
    (@user_id, @housing_category_id,       76000, '2026-08-01', 'FIXED',    '8月分家賃'),
    (@user_id, @communication_category_id,  3980, '2026-08-03', 'FIXED',    'スマートフォン料金'),
    (@user_id, @communication_category_id,  4180, '2026-08-04', 'FIXED',    '自宅インターネット料金'),
    (@user_id, @food_category_id,           4920, '2026-08-05', 'VARIABLE', '食材まとめ買い'),
    (@user_id, @transportation_category_id, 5000, '2026-08-07', 'VARIABLE', '交通系ICカードへチャージ'),
    (@user_id, @utility_category_id,        7024, '2026-08-09', 'FIXED',    '電気料金'),
    (@user_id, @daily_goods_category_id,    2740, '2026-08-11', 'VARIABLE', '日用品まとめ買い'),
    (@user_id, @food_category_id,           4390, '2026-08-13', 'VARIABLE', '食材まとめ買い'),
    (@user_id, @entertainment_category_id,  4600, '2026-08-15', 'VARIABLE', 'イベント・飲み物'),
    (@user_id, @utility_category_id,        4150, '2026-08-17', 'FIXED',    'ガス・水道料金'),
    (@user_id, @food_category_id,           5150, '2026-08-19', 'VARIABLE', '食材とプロテイン'),
    (@user_id, @transportation_category_id, 3460, '2026-08-21', 'VARIABLE', '休日の電車代'),
    (@user_id, @other_category_id,          2200, '2026-08-23', 'VARIABLE', 'クリーニング代'),
    (@user_id, @salary_category_id,       230000, '2026-08-25', NULL,       '8月分給与'),
    (@user_id, @food_category_id,           5560, '2026-08-27', 'VARIABLE', '食材とコーヒー豆'),
    (@user_id, @daily_goods_category_id,    2380, '2026-08-29', 'VARIABLE', 'キッチン消耗品'),
    (@user_id, @entertainment_category_id,  2850, '2026-08-31', 'VARIABLE', '書籍・動画配信');

COMMIT;

-- 登録結果の確認
SELECT
    DATE_FORMAT(t.transaction_date, '%Y-%m') AS target_month,
    c.transaction_type,
    SUM(t.amount) AS total_amount
FROM transactions t
INNER JOIN categories c
    ON c.id = t.category_id
   AND c.user_id = t.user_id
WHERE t.user_id = @user_id
  AND t.transaction_date BETWEEN '2026-06-01' AND '2026-08-31'
GROUP BY
    DATE_FORMAT(t.transaction_date, '%Y-%m'),
    c.transaction_type
ORDER BY
    target_month,
    c.transaction_type;

