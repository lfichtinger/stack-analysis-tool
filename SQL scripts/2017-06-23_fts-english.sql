-- create new tsvector with configuration 'english'
/*ALTER TABLE posts add column tsv_en tsvector;
ALTER TABLE posts add column tsv_en_answers tsvector;
ALTER TABLE posts add column tsv_en_comments tsvector;
CREATE INDEX tsv_en_idx ON posts using gin(tsv_en);
CREATE INDEX tsv_en_answers_idx ON posts using gin(tsv_en_answers);
CREATE INDEX tsv_en_comments_idx ON posts using gin(tsv_en_comments); */

UPDATE posts SET tsv_en = setweight(to_tsvector('english', coalesce(data->>'title', '')), 'A')
	  || setweight(to_tsvector('english', coalesce(data->>'body', '')), 'B'),
  tsv_en_answers = setweight(to_tsvector('english', coalesce(getanswertext(id), '')), 'C'),
  tsv_en_comments = setweight(to_tsvector('english', coalesce(getcommenttext(id), '')), 'D') 
      || setweight(to_tsvector('english', coalesce(getanswercommenttext(id), '')), 'D') ;
    