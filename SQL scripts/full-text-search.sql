ALTER TABLE posts add column tsv tsvector;
ALTER TABLE posts add column tsv_answers tsvector;
ALTER TABLE posts add column tsv_comments tsvector;

-- ad on 02.08.
ALTER TABLE posts add column tsv_all tsvector;

CREATE INDEX tsv_idx ON posts using gin(tsv);
CREATE INDEX tsv_answers_idx ON posts using gin(tsv_answers);
CREATE INDEX tsv_comments_idx ON posts using gin(tsv_comments);

-- add on 02.08.
CREATE INDEX tsv_all_idx ON posts using gin(tsv_comments);

-- FUNCTION: public.getcommenttext(integer)
CREATE OR REPLACE FUNCTION public.getcommenttext(
	pid integer)
    RETURNS text
    LANGUAGE 'plpgsql'
AS $function$

declare 
	coms text[]; 
begin
	select array(select trim(elem->>'body'::text, '"') from jsonb_array_elements(p.data->'comments') elem)
    into coms
    from posts p
    where p.id = pid;
    
    return array_to_string(coms, ', ', '');
end;

$function$;

-- FUNCTION: public.getanswertext(integer)
CREATE OR REPLACE FUNCTION public.getanswertext(
	pid integer)
    RETURNS text
    LANGUAGE 'plpgsql'
AS $function$

declare 
	coms text[]; 
begin
	select array(select trim(elem->>'body'::text, '"') from jsonb_array_elements(p.data->'answers') elem)
    into coms
    from posts p
    where p.id = pid;
    
    return array_to_string(coms, ', ', '');
end;

$function$;

-- FUNCTION: public.getanswercommenttext(integer)
CREATE OR REPLACE FUNCTION public.getanswercommenttext(
	pid integer)
    RETURNS text
    LANGUAGE 'plpgsql'
AS $function$

declare coms text[]; 
begin
	select array(select trim(elem->>'body'::text, '"') from jsonb_array_elements(p.data->'answers') ans, jsonb_array_elements(ans->'comments') elem)
    into coms
    from posts p
    where p.id = pid;
    
    return array_to_string(coms, ', ', '');
end;

$function$;


UPDATE posts SET tsv = setweight(to_tsvector(coalesce(data->>'title', '')), 'A')
	|| setweight(to_tsvector(coalesce(data->>'body', '')), 'B'),
    tsv_answers = setweight(to_tsvector(coalesce(getanswertext(id), '')), 'A'),
    tsv_comments = setweight(to_tsvector(coalesce(getcommenttext(id), '')), 'A') || setweight(to_tsvector(coalesce(getanswercommenttext(id), '')), 'A') ;
    

UPDATE posts SET tsv_all = setweight(to_tsvector(coalesce(data->>'title', '')), 'A')
	|| setweight(to_tsvector(coalesce(data->>'body', '')), 'B')
    || setweight(to_tsvector(coalesce(getanswertext(id), '')), 'B')
    || setweight(to_tsvector(coalesce(getcommenttext(id), '')), 'C') || setweight(to_tsvector(coalesce(getanswercommenttext(id), '')), 'C') ;
