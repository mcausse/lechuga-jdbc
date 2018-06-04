package org.frijoles.jdbc.util;

import java.util.Arrays;
import java.util.List;

import org.frijoles.jdbc.DataAccesFacade;
import org.frijoles.jdbc.queryobject.Query;

public class SqlScriptExecutor {

    public static final String DEFAULT_CHARSETNAME = "UTF-8";

    final DataAccesFacade facade;

    public SqlScriptExecutor(final DataAccesFacade facade) {
        super();
        this.facade = facade;
    }

    public void runFromClasspath(final String classPathFileName) {
        runFromClasspath(classPathFileName, DEFAULT_CHARSETNAME);
    }

    public void runFromClasspath(final String classPathFileName, final String charSetName) {
        final String text = FileUtils.loadFileFromClasspath(classPathFileName, charSetName);
        final List<String> stms = new SqlStatementParser(text).getStatementList();

        execute(stms);
    }

    public void execute(final List<String> stms) {
        for (final String stm : stms) {
            facade.update(Query.immutable(stm));
        }
    }

    public void execute(final String... stms) {
        execute(Arrays.asList(stms));
    }

}
