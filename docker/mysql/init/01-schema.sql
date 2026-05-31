CREATE TABLE orders (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        user_name VARCHAR(100) NOT NULL,
                        product_name VARCHAR(100) NOT NULL,
                        category VARCHAR(50) NOT NULL,
                        amount INT NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        order_date TIMESTAMP NOT NULL,
                        PRIMARY KEY (id)
);

CREATE TABLE excel_jobs (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            status VARCHAR(20) NOT NULL,
                            requested_at TIMESTAMP NOT NULL,
                            started_at TIMESTAMP NULL,
                            completed_at TIMESTAMP NULL,
                            file_path VARCHAR(500) NULL,
                            error_message VARCHAR(1000) NULL,
                            PRIMARY KEY (id)
);