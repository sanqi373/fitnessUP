CREATE TABLE IF NOT EXISTS user_check_in (
    user_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    PRIMARY KEY (user_id, check_in_date),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS shop_cart_line (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    paid_amount DECIMAL(16,2),
    purchased_at DATETIME(3),
    open_product_id INT GENERATED ALWAYS AS (IF(paid_amount IS NULL, product_id, NULL)) STORED,
    UNIQUE KEY one_cart_line (user_id, open_product_id),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS wallet_top_up (
    user_id BIGINT NOT NULL,
    request_id CHAR(32) NOT NULL,
    amount DECIMAL(14,2) NOT NULL CHECK (amount > 0),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (user_id, request_id),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS workout_session (
    user_id BIGINT NOT NULL,
    session_id CHAR(36) NOT NULL,
    plan_id BIGINT NOT NULL,
    elapsed_ms BIGINT NOT NULL DEFAULT 0,
    running_since_ms BIGINT,
    finished_at DATETIME(3),
    open_plan_id BIGINT GENERATED ALWAYS AS (IF(finished_at IS NULL, plan_id, NULL)) STORED,
    PRIMARY KEY (user_id, session_id),
    UNIQUE KEY one_open_workout (user_id, open_plan_id),
    INDEX workout_totals (user_id, plan_id, finished_at),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS workout_session_day (
    user_id BIGINT NOT NULL,
    session_id CHAR(36) NOT NULL,
    workout_date DATE NOT NULL,
    elapsed_ms BIGINT NOT NULL DEFAULT 0,
    estimated_kcal DECIMAL(26,9) NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, session_id, workout_date),
    INDEX daily_workout_totals (user_id, workout_date),
    FOREIGN KEY (user_id, session_id) REFERENCES workout_session(user_id, session_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS saved_training_plan (
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    saved_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_id, plan_id),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES training_plan(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS body_record (
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    revision INT NOT NULL DEFAULT 1,
    height DECIMAL(7,1),
    weight DECIMAL(7,1),
    body_fat DECIMAL(7,1),
    waist DECIMAL(7,1),
    basal_metabolism DECIMAL(7,1),
    carbs DECIMAL(7,1),
    protein DECIMAL(7,1),
    fat DECIMAL(7,1),
    water DECIMAL(7,1),
    PRIMARY KEY (user_id, record_date),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS saved_diet_plan (
    user_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    saved_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_id, recipe_id),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (recipe_id) REFERENCES diet_recommendation(id) ON DELETE CASCADE
) ENGINE=InnoDB;
