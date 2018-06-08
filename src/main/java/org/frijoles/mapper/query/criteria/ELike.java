package org.frijoles.mapper.query.criteria;

public enum ELike {

    /**
     * A -- A
     */
    EXACT_MATCH(false, false),

    /**
     * A -- A%
     */
    BEGINS_WITH(false, true),

    /**
     * A -- %A%
     */
    CONTAINS(true, true),

    /**
     * A -- %A
     */
    ENDS_WITH(true, false);

    private final boolean beginWithWildchar;
    private final boolean endsWithWildchar;

    private ELike(final boolean beginWithWildchar, final boolean endsWithWildchar) {
        this.beginWithWildchar = beginWithWildchar;
        this.endsWithWildchar = endsWithWildchar;
    }

    /**
     * renderitza la "regexp" SQL
     */
    public String process(final String value) {
        String str = value;
        if (beginWithWildchar) {
            str = '%' + str;
        }
        if (endsWithWildchar) {
            str = str + '%';
        }
        return str;
    }

}