SET SESSION cte_max_recursion_depth = 100000;

INSERT INTO orders (
    user_name,
    product_name,
    category,
    amount,
    status,
    order_date
)
WITH RECURSIVE numbers AS (
    SELECT 1 AS number

    UNION ALL

    SELECT number + 1
    FROM numbers
    WHERE number < 100000
)
SELECT
    CONCAT('user_', number),
    CONCAT('product_', MOD(number, 100) + 1),
    CASE MOD(number, 3)
        WHEN 0 THEN 'drawing'
        WHEN 1 THEN 'stationery'
        ELSE 'craft'
        END,
    (MOD(number, 100) + 1) * 1000,
    CASE MOD(number, 3)
        WHEN 0 THEN 'confirmed'
        WHEN 1 THEN 'cancelled'
        ELSE 'pending'
        END,
    TIMESTAMPADD(SECOND, -number, CURRENT_TIMESTAMP)
FROM numbers;