drop table tex if exists;
drop table exp if exists;


create table exp (
 cod_pos varchar(100)	/* id.codPos */ ,
 any_exp smallint	/* id.anyExp */ ,
 num_exp integer	/* id.numExp */ ,
 descr varchar(100)	/* desc */ 
);
alter table exp add constraint pk_exp primary key (cod_pos,any_exp,num_exp);
create table tex (
 id_tex integer	/* idTex */ ,
 textx varchar(100)	/* text */ ,
 cod_posx varchar(100)	/* codPosx */ ,
 any_expx smallint	/* anyExpx */ ,
 num_expx integer	/* numExpx */ 
);
alter table tex add constraint pk_tex primary key (id_tex);
drop sequence seq_tex if exists;
create sequence seq_tex start with 100;


alter table tex add foreign key (cod_posx,any_expx,num_expx) references exp (cod_pos,any_exp,num_exp);

