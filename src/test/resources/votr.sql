drop table votacions if exists;
drop table opcions if exists;
drop table usrs if exists;
drop table msgs if exists;


create table votacions (
 hash_votacio varchar(100)	/* hashVotacio */ ,
 data_creacio timestamp	/* dataCreacio */ ,
 data_fi timestamp	/* dataFi */ ,
 descripcio varchar(100)	/* descripcio */ ,
 titol varchar(100)	/* titol */ 
);
alter table votacions add constraint pk_votacions primary key (hash_votacio);
create table opcions (
 hash_votacio varchar(100)	/* idOpcio.hashVotacio */ ,
 num smallint	/* idOpcio.num */ ,
 descripcio varchar(100)	/* descripcio */ ,
 titol varchar(100)	/* titol */ 
);
alter table opcions add constraint pk_opcions primary key (hash_votacio,num);
create table usrs (
 hash_usr varchar(100)	/* hashUsr */ ,
 alias varchar(100)	/* alias */ ,
 email varchar(100)	/* email */ ,
 hash_votacio varchar(100)	/* hashVotacio */ ,
 num_opcio_votada smallint	/* numOpcioVotada */ 
);
alter table usrs add constraint pk_usrs primary key (hash_usr);
create table msgs (
 id smallint	/* id */ ,
 data timestamp	/* data */ ,
 hash_votacio varchar(100)	/* hashVotacio */ ,
 text varchar(100)	/* text */ 
);
alter table msgs add constraint pk_msgs primary key (id);
drop sequence seq_msg if exists;
create sequence seq_msg start with 100;

