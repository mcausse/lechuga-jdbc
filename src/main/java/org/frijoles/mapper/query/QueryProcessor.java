package org.frijoles.mapper.query;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.frijoles.jdbc.queryobject.Query;
import org.frijoles.jdbc.queryobject.QueryObject;
import org.frijoles.mapper.Column;
import org.frijoles.mapper.TableModel;

public class QueryProcessor {

    public QueryProcessor() {
        super();
    }

    public QueryObject process(final Map<String, TableModel<?>> models, final String input, final Object[] params) {

        final Query r = new Query();
        int currParamIndex = 0;

        final Matcher m = Pattern.compile("\\{([^}]*)\\}").matcher(input);
        final StringBuffer sb = new StringBuffer();
        while (m.find()) {
            final QueryObject value = evaluateExpression(models, m.group(1), params, currParamIndex);
            currParamIndex += value.getArgs().length;

            m.appendReplacement(sb, value.getSql());
            r.addArgsList(value.getArgsList());
        }
        m.appendTail(sb);

        final Query q = new Query();
        q.append(sb.toString());
        q.addArgsList(r.getArgsList());
        return q;
    }

    /**
     * <pre>
     * {d} => DOG
     * {d.name} => NAME
     *
     * {d.name=?}
     * {d.age in (?,?,?)}
     *
     * <exp> = "{" <e> "}"
     * <e>   = TABLEALIAS {"." PROPERTY} [OP]
     *
     *
     * </pre>
     */
    protected QueryObject evaluateExpression(final Map<String, TableModel<?>> models, final String expression,
            final Object[] params, final int currParamIndex) {

        try {

            /*
             * tokenitza expressió
             */
            final StringBuilder tableAlias = new StringBuilder();
            StringBuilder property = null;
            String op = null;

            int i = 0;
            while (i < expression.length() && Character.isLetter(expression.charAt(i))) {
                tableAlias.append(expression.charAt(i));
                i++;
            }

            if (i < expression.length()) {
                if (expression.charAt(i) != '.') {
                    throw new RuntimeException("expected '.' but readed '" + expression.charAt(i) + "'");
                }
                i++; // chupa .

                property = new StringBuilder();
                while (i < expression.length() &&
                /**/(Character.isLetter(expression.charAt(i)) || expression.charAt(i) == '*'
                        || expression.charAt(i) == '.')) {
                    property.append(expression.charAt(i));
                    i++;
                }

                if (i < expression.length()) {
                    op = expression.substring(i);
                }
            }

            /*
             * evalua
             */
            final Query q = new Query();

            if (!models.containsKey(tableAlias.toString())) {
                throw new RuntimeException("model not found for alias: '" + tableAlias + "'");
            }
            final TableModel<?> model = models.get(tableAlias.toString());

            if (property == null) {
                q.append(model.getTableName() + " " + tableAlias.toString());
            } else if (property.toString().equals("*")) {
                q.append(join(tableAlias.toString(), model.getColumnNames()));
            } else {
                final Column p = model.findColumnByName(property.toString());
                if (op == null) {
                    q.append(tableAlias + "." + p.getColumnName());
                } else {
                    q.append(tableAlias + "." + p.getColumnName() + op);
                    int paramIndex = currParamIndex;
                    for (int j = 0; j < op.length(); j++) {
                        if (op.charAt(j) == '?') {
                            q.addArg(p.convertValueForJdbc(params[paramIndex]));
                            paramIndex++;
                        }
                    }
                }

            }

            return q;

        } catch (final RuntimeException e) {
            throw new RuntimeException("error in expression: '" + expression + "'", e);
        }

    }

    String join(final String prefix, final Collection<String> columnNames) {
        final StringBuilder r = new StringBuilder();
        int i = 0;
        for (String v : columnNames) {
            if (i > 0) {
                r.append(',');
            }
            r.append(prefix);
            r.append('.');
            r.append(v);
            i++;
        }
        return r.toString();
    }

}