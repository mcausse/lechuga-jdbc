// package org.lechuga.annotated.anno;
//
// import java.lang.annotation.ElementType;
// import java.lang.annotation.Retention;
// import java.lang.annotation.RetentionPolicy;
// import java.lang.annotation.Target;
//
//// TODO experrimental
// @Retention(RetentionPolicy.RUNTIME)
// @Target(ElementType.TYPE)
// public @interface NamedQuery {
//
// String name();
//
// NamedQueryAlias resultAlias();
//
// boolean uniqueResult();
//
//
// NamedQueryAlias[] aliases();
// String query();
//
// //TODO experrimental
// @Retention(RetentionPolicy.RUNTIME)
// @Target(ElementType.TYPE)
// public static @interface NamedQueryAlias {
//
// String alias();
// Class<?> entityClass();
//
// }
// }