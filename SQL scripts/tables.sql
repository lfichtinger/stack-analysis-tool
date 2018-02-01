-- table for tags
CREATE TABLE public.tags
(
    so_name TEXT NOT NULL,
    so_count        INTEGER,
    so_has_synonyms BOOLEAN,
    so_synonyms TEXT[],
    so_last_activity_date TIMESTAMP,
    so_is_moderator_only BOOLEAN,
    so_is_required       BOOLEAN,
    fetch_date TIMESTAMP NOT NULL,
    query TEXT,
    PRIMARY KEY(so_name, fetch_date)
);

-- table for posts (questions, answers, comments)
CREATE TABLE public.posts
(
    id                           INTEGER NOT NULL,
    fetch_date timestamp without TIME zone NOT NULL,
    data jsonb,
    query_tags text,
    parent_query_tag text,
    CONSTRAINT posts_pkey PRIMARY KEY (id, fetch_date)
); 