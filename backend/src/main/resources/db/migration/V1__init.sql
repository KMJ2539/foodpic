-- V1: Initial schema for foodpic

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    username        VARCHAR(50)  NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(100) NOT NULL,
    bio             TEXT,
    profile_photo_id BIGINT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE food_items (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    brand           VARCHAR(120),
    serving_size_g  NUMERIC(10,2),
    calories_kcal   NUMERIC(10,2),
    protein_g       NUMERIC(10,2),
    fat_g           NUMERIC(10,2),
    carbs_g         NUMERIC(10,2),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_food_items_name_brand UNIQUE (name, brand)
);

CREATE TABLE meal_entries (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    meal_type       VARCHAR(20) NOT NULL,
    eaten_at        TIMESTAMPTZ NOT NULL,
    note            TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT fk_meal_entries_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT ck_meal_entries_meal_type
        CHECK (meal_type IN ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'))
);

CREATE TABLE meal_entry_items (
    id              BIGSERIAL PRIMARY KEY,
    meal_entry_id   BIGINT       NOT NULL,
    food_item_id    BIGINT       NOT NULL,
    quantity        NUMERIC(10,2) NOT NULL,
    unit            VARCHAR(30)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_meal_entry_items_meal_entry
        FOREIGN KEY (meal_entry_id) REFERENCES meal_entries(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_meal_entry_items_food_item
        FOREIGN KEY (food_item_id) REFERENCES food_items(id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_meal_entry_items_quantity_positive CHECK (quantity > 0)
);

CREATE TABLE photo_assets (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    storage_key     VARCHAR(512) NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    width           INT,
    height          INT,
    size_bytes      BIGINT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT uq_photo_assets_storage_key UNIQUE (storage_key),
    CONSTRAINT fk_photo_assets_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_profile_photo
    FOREIGN KEY (profile_photo_id) REFERENCES photo_assets(id)
    ON DELETE SET NULL;

CREATE TABLE feed_posts (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    meal_entry_id   BIGINT,
    photo_asset_id  BIGINT,
    caption         TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT fk_feed_posts_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_feed_posts_meal_entry
        FOREIGN KEY (meal_entry_id) REFERENCES meal_entries(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_feed_posts_photo_asset
        FOREIGN KEY (photo_asset_id) REFERENCES photo_assets(id)
        ON DELETE SET NULL
);

CREATE TABLE follows (
    id              BIGSERIAL PRIMARY KEY,
    follower_id     BIGINT      NOT NULL,
    following_id    BIGINT      NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_follows_follower_following UNIQUE (follower_id, following_id),
    CONSTRAINT fk_follows_follower
        FOREIGN KEY (follower_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_follows_following
        FOREIGN KEY (following_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT ck_follows_not_self CHECK (follower_id <> following_id)
);

CREATE TABLE post_likes (
    id              BIGSERIAL PRIMARY KEY,
    post_id         BIGINT      NOT NULL,
    user_id         BIGINT      NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_post_likes_post_user UNIQUE (post_id, user_id),
    CONSTRAINT fk_post_likes_post
        FOREIGN KEY (post_id) REFERENCES feed_posts(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE post_comments (
    id                  BIGSERIAL PRIMARY KEY,
    post_id             BIGINT      NOT NULL,
    user_id             BIGINT      NOT NULL,
    parent_comment_id   BIGINT,
    content             TEXT        NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ,
    CONSTRAINT fk_post_comments_post
        FOREIGN KEY (post_id) REFERENCES feed_posts(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_post_comments_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_post_comments_parent
        FOREIGN KEY (parent_comment_id) REFERENCES post_comments(id)
        ON DELETE SET NULL,
    CONSTRAINT ck_post_comments_status
        CHECK (status IN ('ACTIVE', 'DELETED_BY_USER', 'DELETED_BY_MODERATOR'))
);

CREATE TABLE blocks (
    id              BIGSERIAL PRIMARY KEY,
    blocker_id      BIGINT      NOT NULL,
    blocked_id      BIGINT      NOT NULL,
    reason          VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_blocks_blocker_blocked UNIQUE (blocker_id, blocked_id),
    CONSTRAINT fk_blocks_blocker
        FOREIGN KEY (blocker_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_blocks_blocked
        FOREIGN KEY (blocked_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT ck_blocks_not_self CHECK (blocker_id <> blocked_id)
);

CREATE TABLE reports (
    id                  BIGSERIAL PRIMARY KEY,
    reporter_id         BIGINT       NOT NULL,
    target_user_id      BIGINT,
    target_post_id      BIGINT,
    target_comment_id   BIGINT,
    reason              VARCHAR(255) NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    detail              TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    reviewed_at         TIMESTAMPTZ,
    CONSTRAINT fk_reports_reporter
        FOREIGN KEY (reporter_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_reports_target_user
        FOREIGN KEY (target_user_id) REFERENCES users(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_reports_target_post
        FOREIGN KEY (target_post_id) REFERENCES feed_posts(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_reports_target_comment
        FOREIGN KEY (target_comment_id) REFERENCES post_comments(id)
        ON DELETE SET NULL,
    CONSTRAINT ck_reports_status
        CHECK (status IN ('PENDING', 'RESOLVED', 'REJECTED')),
    CONSTRAINT ck_reports_any_target
        CHECK (
            (CASE WHEN target_user_id IS NOT NULL THEN 1 ELSE 0 END) +
            (CASE WHEN target_post_id IS NOT NULL THEN 1 ELSE 0 END) +
            (CASE WHEN target_comment_id IS NOT NULL THEN 1 ELSE 0 END)
            >= 1
        )
);

-- Feed cursor index for keyset pagination
CREATE INDEX idx_feed_posts_created_at_id_desc
    ON feed_posts (created_at DESC, id DESC);

CREATE INDEX idx_feed_posts_user_created_at_desc ON feed_posts (user_id, created_at DESC);
CREATE INDEX idx_meal_entries_user_eaten_at_desc ON meal_entries (user_id, eaten_at DESC);
CREATE INDEX idx_post_comments_post_created_at_desc ON post_comments (post_id, created_at DESC);
CREATE INDEX idx_post_likes_post ON post_likes (post_id);
CREATE INDEX idx_follows_following ON follows (following_id);
CREATE TABLE IF NOT EXISTS app_metadata (
    id BIGSERIAL PRIMARY KEY,
    key VARCHAR(100) NOT NULL UNIQUE,
    value VARCHAR(255) NOT NULL
);
