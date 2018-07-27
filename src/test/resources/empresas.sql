drop table empresa_direccion if exists;
drop table empleados if exists;
drop table empresas if exists;


create table empresas (
 id_empresa integer	/* idEmpresa */ ,
 nombre varchar(100)	/* nombre */ 
);
alter table empresas add constraint pk_empresas primary key (id_empresa);
drop sequence seq_empresas if exists;
create sequence seq_empresas start with 100;
create table empleados (
 id_direccion smallint	/* idDireccion */ ,
 nombre varchar(100)	/* nombre */ 
);
alter table empleados add constraint pk_empleados primary key (id_direccion);
drop sequence seq_empleados if exists;
create sequence seq_empleados start with 100;
create table empresa_direccion (
 id_empresa integer	/* id.idEmpresa */ ,
 id_direccion smallint	/* id.idDireccion */ ,
 tipo_direccion varchar(20)	/* tipoDireccion */ 
);
alter table empresa_direccion add constraint pk_empresa_direccion primary key (id_empresa,id_direccion);


alter table empresa_direccion add foreign key (id_empresa) references empresas (id_empresa);
alter table empresa_direccion add foreign key (id_direccion) references empleados (id_direccion);