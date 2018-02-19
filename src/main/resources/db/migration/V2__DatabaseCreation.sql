CREATE TABLE tweets_information
(
    id       SERIAL NOT NULL,
    tweet_id CHARACTER VARYING(50) UNIQUE,
    user_id  CHARACTER VARYING(50),
    lon      NUMERIC,
    lat      NUMERIC,
    geog     GEOGRAPHY
);


CREATE INDEX idx_tweets_information_location
    ON tweets_information
    USING GIST (geog);
