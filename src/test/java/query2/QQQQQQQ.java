// package query2;
//
// import java.util.LinkedHashMap;
// import java.util.Map;
//
// import org.frijoles.jdbc.DataAccesFacade;
// import org.frijoles.jdbc.queryobject.SimpleQuery;
// import org.frijoles.mapper.TableModel;
// import org.frijoles.mapper.query.QueryProcessor;
//
/// **
// * <pre>
// * TODO TODO TODO TODO TODO
// *
// * <exp> ::= PREFIX ".*"
// * <exp> ::= PREFIX "." PROP {"." PROP} REST
// * <exp> ::= <className> " " PREFIX
// * <className> ::= PROP {"." PROP}
// * <className> ::= "this"
// *
// * select {t.*} from {this t} where {t.id=?}
// *
// *
// * select {p.name} as {this.key}, sum({i.price}) as {this.value}
// * from {org.frijoles.anno.Pizza p} join {org.frijoles.anno.Ingredient i}
// * on {p.id}={i.idPizza}
// * group by {p.name}
// *
// * TODO TODO TODO TODO TODO
// * </pre>
// */
// public class QQQQQQQ {
//
// public static class QB<E> {
//
// final DataAccesFacade facade;
// final TableModel<E> resultEntityModel;
// final String tableAlias;
//
// final Map<String, TableModel<?>> models;
//
// public QB(DataAccesFacade facade, TableModel<E> resultEntityModel, String
// tableAlias,
// Map<String, TableModel<?>> models) {
// super();
// this.facade = facade;
// this.resultEntityModel = resultEntityModel;
// this.tableAlias = tableAlias;
//// this.q = new SimpleQuery();
// this.models = new LinkedHashMap<String, TableModel<?>>();
//// this.replacer = new QueryProcessor();
//
// if (tableAlias != null) {
// this.models.put(tableAlias, resultEntityModel);
// }
// }
//
// }
// }
