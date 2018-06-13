drop table imputacions if exists;
drop table users if exists;
drop table tasques if exists;
drop table projects if exists;


create table projects (
 id_project integer	/* idProject */ ,
 name varchar(100)	/* name */ 
);
alter table projects add constraint pk_projects primary key (id_project);
drop sequence seq_projects if exists;
create sequence seq_projects start with 100;
create table tasques (
 id_project integer	/* id.idProject */ ,
 nom_tasca varchar(100)	/* id.nomTasca */ 
);
alter table tasques add constraint pk_tasques primary key (id_project,nom_tasca);
create table users (
 id_user smallint	/* idUser */ ,
 email varchar(100)	/* email */ 
);
alter table users add constraint pk_users primary key (id_user);
alter table users alter column id_user smallint generated by default as identity(start with 100);
create table imputacions (
 dia varchar(100)	/* id.dia */ ,
 id_project integer	/* id.idProject */ ,
 id_user smallint	/* id.idUser */ ,
 nom_tasca varchar(100)	/* id.nomTasca */ ,
 desc varchar(100)	/* desc */ ,
 hores float	/* hores */ 
);
alter table imputacions add constraint pk_imputacions primary key (dia,id_project,id_user,nom_tasca);







alter table tasques add foreign key (id_project) references projects(id_project);
alter table imputacions add foreign key (id_project) references projects(id_project);
alter table imputacions add foreign key (id_project,nom_tasca) references tasques(id_project,nom_tasca);
