DROP TABLE IF EXISTS review_rating;
create table review_rating (
	review_id bigint not null auto_increment, 
	restaurant_id bigint not null, 
	customer_email varchar(255) not null, 
	comments varchar(255), 
	rating float not null,
	reviewed_date datetime not null,
	like_count bigint,	
	dislike_count bigint,
	primary key (review_id));