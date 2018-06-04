// package org.frijoles.mapper;
//
// import java.math.BigDecimal;
// import java.util.Date;
// import java.util.LinkedHashMap;
// import java.util.Map;
// import java.util.Map.Entry;
//
// import org.frijoles.mapper.Column;
// import org.frijoles.mapper.TableModel;
//
//
// public class HsqldbDDLGenerator {
//
// final TableModel<?> em;
// final Map<String, Column> properties;
// final Map<String, Column> idproperties;
//
// HsqldbDDLGenerator(TableModel<?> em) {
// super();
// this.em = em;
// this.properties = new LinkedHashMap<>();
// curse(em.getId(), "", properties);
//
// this.idproperties = new LinkedHashMap<>(properties);
//
// for (Property p : em.getRegulars()) {
// curse(p, "", properties);
// }
// }
//
// protected void curse(Property p, String path, Map<String, ScalarProperty> r)
// {
//
// if (p instanceof ScalarProperty) {
// String propertyPath = (path + "." + p.getPropertyName()).substring(1);
// r.put(propertyPath, (ScalarProperty) p);
// } else {
// CompositeProperty p2 = (CompositeProperty) p;
// for (Property c : p2.getChilds()) {
// curse(c, path + "." + p.getPropertyName(), r);
// }
// }
// }
//
// public static String generateScript(Class<?>... classes) {
//
// StringBuilder r = new StringBuilder();
//
// for (Class<?> c : classes) {
// IEntityManagerFactory emf = new CocEntityManagerFactory(null);
// EntityModel ec = emf.getEntityModelFor(c);
// HsqldbDDLGenerator gen = new HsqldbDDLGenerator(ec);
// r.append(gen.generateDrops());
// }
// r.append("\n\n");
// for (Class<?> c : classes) {
// IEntityManagerFactory emf = new CocEntityManagerFactory(null);
// EntityModel ec = emf.getEntityModelFor(c);
// HsqldbDDLGenerator gen = new HsqldbDDLGenerator(ec);
// r.append(gen.generateCreates());
// }
//
// return r.toString();
// }
//
// protected String generateDrops() {
// StringBuilder r = new StringBuilder();
// r.append(generateDropTable());
// return r.toString();
// }
//
// protected String generateCreates() {
// StringBuilder r = new StringBuilder();
// r.append(generateCreateTable());
// r.append(generateAddPks());
// r.append(generateAutogenerated());
// return r.toString();
// }
//
// protected String generatePropertyConstants() {
// StringBuilder r = new StringBuilder();
// String constantsClassName =
// StringUtils.sqlCaseToUpperCamelCase(em.getTableName()) + "_";
// r.append("public interface " + constantsClassName + " {\n");
// for (Entry<String, ScalarProperty> p : properties.entrySet()) {
// r.append(" public static final String ");
// r.append(p.getValue().getColumnName().toUpperCase());
// r.append(" = \"");
// r.append(p.getKey());
// r.append("\";\n");
// }
// r.append("}\n");
// return r.toString();
// }
//
// protected String generateAutogenerated() {
// StringBuilder r = new StringBuilder();
// for (ScalarProperty p : properties.values()) {
// if (p.getGenerator() != null) {
// if (p.getGenerator() instanceof HsqldbIdentity) {
// r.append(String
// .format("alter table %s alter column %s %s generated by default as
// identity(start with 100);%n",
// em.getTableName(), p.getColumnName(), sqlTypeOf(p.getType())));
// } else if (p.getGenerator() instanceof HsqldbSequence) {
//
// HsqldbSequence seq = (HsqldbSequence) p.getGenerator();
// r.append("drop sequence " + seq.getSequenceName() + " if exists;\n");
// r.append("create sequence " + seq.getSequenceName() + " start with 100;\n");
//
// } else {
// r.append("-- TODO " + p.getGenerator().getClass().getName());
// }
// }
// }
// return r.toString();
// }
//
// protected String generateDropTable() {
// return "drop table " + em.getTableName() + " if exists;\n";
// }
//
// protected String generateCreateTable() {
// StringBuilder r = new StringBuilder();
// r.append("create table " + em.getTableName() + " (\n");
// int c = 0;
// for (Entry<String, ScalarProperty> e : properties.entrySet()) {
// if (c > 0) {
// r.append(",\n");
// }
// c++;
// r.append(" ");
// r.append(e.getValue().getColumnName());
// r.append(" ");
// r.append(sqlTypeOf(e.getValue().getType()));
//
// r.append("\t/* " + e.getKey() + " */ ");
// }
// r.append("\n);\n");
// return r.toString();
// }
//
// protected String generateAddPks() {
// StringBuilder r = new StringBuilder();
// r.append("alter table " + em.getTableName());
// r.append(" add constraint pk_" + em.getTableName());
// r.append(" primary key (");
//
// int c = 0;
// for (ScalarProperty e : idproperties.values()) {
// if (c > 0) {
// r.append(",");
// }
// c++;
// r.append(e.getColumnName());
// }
//
// r.append(");\n");
// return r.toString();
// }
//
// // protected String sqlTypeOf(Class<?> type) {
// //
// // if (Number.class.isAssignableFrom(type)) {
// // if (type.equals(Byte.class) || type.equals(Short.class)) {
// // return "TINYINT";
// // } else if (type.equals(Integer.class)) {
// // return "SMALLINT";
// // } else if (type.equals(Long.class)) {
// // return "INTEGER";
// // }
// // if (type.equals(Float.class) || type.equals(Double.class) ||
// type.equals(BigDecimal.class))
//
// {
// // return "DECIMAL";
// // }
// // } else if (Date.class.isAssignableFrom(type)) {
// // return "TIMESTAMP";
// // } else if (type.equals(String.class)) {
// // return "VARCHAR(100)";
// // } else if (type.equals(Boolean.class)) {
// // return "BIT";
// // } else if (type.isEnum()) {
// // return "VARCHAR(20)";
// // }
// //
// // return "UNKNOWN";
// // }
//
// private static String sqlTypeOf(Class<?> type) throws
// LentejaConfigurationException {
//
// if (type.isPrimitive()) {
// if (type == int.class) {
// return "smallint";
// }
// if (type == long.class) {
// return "integer";
// }
// if (type == short.class) {
// return "tinyint";
// }
// if (type == byte.class) {
// return "tinyint";
// }
// if (type == float.class) {
// return "float";
// }
// if (type == double.class) {
// return "decimal";
// }
// if (type == boolean.class) {
// return "bit";
// }
// if (type == char.class) {
// return "varchar(1)";
// }
// } else if (Number.class.isAssignableFrom(type)) {
// if (type == Integer.class) {
// return "smallint";
// }
// if (type == Long.class) {
// return "integer";
// }
// if (type == Short.class) {
// return "tinyint";
// }
// if (type == Byte.class) {
// return "tinyint";
// }
// if (type == Float.class) {
// return "float";
// }
// if (type == Double.class) {
// return "decimal";
// }
// if (type == BigDecimal.class) {
// return "decimal";
// }
// } else if (type.isEnum()) {
// return "varchar(20)";
// } else {
// if (type == String.class) {
// return "varchar(100)";
// }
// if (type == Date.class) {
// return "timestamp";
// }
// if (type == BigDecimal.class) {
// return "decimal";
// }
// if (type == byte[].class) {
// return "binary(100)";
// }
// if (type == Boolean.class) {
// return "bit";
// }
// if (type == Character.class) {
// return "varchar(1)";
// }
// }
//
// throw new LentejaConfigurationException("unsupported column type: " +
// type.getName());
// }
//
// }
