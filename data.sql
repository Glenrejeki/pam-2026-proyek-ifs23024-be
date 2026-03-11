-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Users
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    bio TEXT NULL,
    photo VARCHAR(255) NULL,
    header_photo VARCHAR(255) NULL,
    location VARCHAR(100) NULL,
    website VARCHAR(255) NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    followers_count INTEGER NOT NULL DEFAULT 0,
    following_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

-- Tweets
CREATE TABLE IF NOT EXISTS tweets (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    image_url TEXT NULL,
    retweet_of_id UUID NULL,
    quote_of_id UUID NULL,
    reply_to_id UUID NULL,
    language VARCHAR(10) NOT NULL DEFAULT 'id',
    likes_count INTEGER NOT NULL DEFAULT 0,
    retweets_count INTEGER NOT NULL DEFAULT 0,
    replies_count INTEGER NOT NULL DEFAULT 0,
    quotes_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

-- Follows
CREATE TABLE IF NOT EXISTS follows (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id UUID NOT NULL REFERENCES users(id),
    following_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL
    );

-- Likes
CREATE TABLE IF NOT EXISTS likes (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    tweet_id UUID NOT NULL REFERENCES tweets(id),
    created_at TIMESTAMP NOT NULL
    );

-- Bookmarks
CREATE TABLE IF NOT EXISTS bookmarks (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    tweet_id UUID NOT NULL REFERENCES tweets(id),
    created_at TIMESTAMP NOT NULL
    );

-- Hashtags
CREATE TABLE IF NOT EXISTS hashtags (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tag VARCHAR(100) NOT NULL UNIQUE,
    count INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL
    );

-- Tweet Hashtags
CREATE TABLE IF NOT EXISTS tweet_hashtags (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tweet_id UUID NOT NULL REFERENCES tweets(id),
    hashtag_id UUID NOT NULL REFERENCES hashtags(id)
    );

-- Refresh Tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    refresh_token TEXT NOT NULL,
    auth_token TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
    );