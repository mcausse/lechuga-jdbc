drop table ingredients if exists;
drop table pizzas if exists;

create table pizzas (
	id integer, 
	name varchar(20)
);
alter table pizzas add constraint pk_pizzas_id primary key (id);
drop sequence seq_pizza if exists;
create sequence seq_pizza start with 100;



create table ingredients (
    id smallint,
    name varchar(20),
    price double,
    id_pizza integer
);
alter table ingredients add constraint pk_ingredients_id primary key (id);
alter table ingredients add foreign key (id_pizza) references pizzas(id);

drop sequence seq_ingredients if exists;
create sequence seq_ingredients start with 100;




