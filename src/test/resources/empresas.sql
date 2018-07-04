drop table empresa_empleado if exists;
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
 id_empleado smallint	/* idEmpleado */ ,
 nombre varchar(100)	/* nombre */ 
);
alter table empleados add constraint pk_empleados primary key (id_empleado);
drop sequence seq_empleados if exists;
create sequence seq_empleados start with 100;
create table empresa_empleado (
 id_empresa integer	/* id.idEmpresa */ ,
 id_empleado smallint	/* id.idEmpleado */ ,
 rol_empleado varchar(20)	/* rolEmpleado */ 
);
alter table empresa_empleado add constraint pk_empresa_empleado primary key (id_empresa,id_empleado);


alter table empresa_empleado add foreign key (id_empresa) references empresas (id_empresa);
alter table empresa_empleado add foreign key (id_empleado) references empleados (id_empleado);

