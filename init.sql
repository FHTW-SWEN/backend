-- =============================================================
--  TourPlanner – PostgreSQL Init Script
--  Matches: Spring Boot backend + Angular frontend
--  Tables: users, tours, tour_logs
-- =============================================================

-- -------------------------------------------------------------
-- 1. USERS
--    Required by spec: self-register + login with credentials
--    Tours/Logs belong to a single user (no sharing)
-- -------------------------------------------------------------
GRANT ALL ON SCHEMA public TO tourplanner;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO tourplanner;

CREATE TABLE IF NOT EXISTS users (
                                     id            BIGSERIAL       PRIMARY KEY,
                                     username      VARCHAR(100)    NOT NULL UNIQUE,
                                     email         VARCHAR(255)    NOT NULL UNIQUE,
                                     password_hash VARCHAR(255)    NOT NULL,           -- store BCrypt hash, never plaintext
                                     created_at    TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- 2. TOURS
--    Required fields per spec:
--      name, description, from, to, transport_type,
--      distance, estimated_time
--    Computed fields (popularity, child_friendliness) are
--      derived at runtime – NOT stored here
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tours (
                                     id              BIGSERIAL       PRIMARY KEY,
                                     user_id         BIGINT,
                                     name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    from_location   VARCHAR(255)    NOT NULL,
    to_location     VARCHAR(255)    NOT NULL,
    transport_type  VARCHAR(50)     NOT NULL,
    distance        DOUBLE PRECISION,
    estimated_time  INTEGER,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
    );

-- -------------------------------------------------------------
-- 3. TOUR LOGS
--    Required fields per spec:
--      date/time, comment, difficulty, total_distance,
--      total_time, rating
--    Multiple logs per tour (many-to-one)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tour_logs (
                                         id              BIGSERIAL       PRIMARY KEY,
                                         tour_id         BIGINT          NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    date_time       TIMESTAMP       NOT NULL,
    comment         TEXT,
    difficulty      SMALLINT        NOT NULL CHECK (difficulty BETWEEN 1 AND 5),
    total_distance  DOUBLE PRECISION NOT NULL,         -- km actually walked/driven
    total_time      INTEGER         NOT NULL,          -- minutes actually spent
    rating          SMALLINT        NOT NULL CHECK (rating BETWEEN 1 AND 5),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
    );

-- -------------------------------------------------------------
-- 4. INDEXES  (speed up common queries)
-- -------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_tours_user_id        ON tours(user_id);
CREATE INDEX IF NOT EXISTS idx_tour_logs_tour_id    ON tour_logs(tour_id);

-- Full-text search index on tours (name + description + from + to)
CREATE INDEX IF NOT EXISTS idx_tours_fts ON tours
    USING GIN (to_tsvector('english', coalesce(name,'') || ' ' ||
    coalesce(description,'') || ' ' ||
    coalesce(from_location,'') || ' ' ||
    coalesce(to_location,'')));

-- Full-text search index on tour_logs (comment)
CREATE INDEX IF NOT EXISTS idx_tour_logs_fts ON tour_logs
    USING GIN (to_tsvector('english', coalesce(comment,'')));

-- -------------------------------------------------------------
-- 5. TOUR PHOTOS
--    User-uploaded photo collection per tour
--    Multiple photos per tour (many-to-one), cascade delete with tour
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tour_photos (
                                           id              BIGSERIAL       PRIMARY KEY,
                                           tour_id         BIGINT          NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
                                           file_name       VARCHAR(255)    NOT NULL,
                                           content_type    VARCHAR(100)    NOT NULL,
                                           data            BYTEA           NOT NULL,
                                           caption         TEXT,
                                           uploaded_at     TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tour_photos_tour_id ON tour_photos(tour_id);
