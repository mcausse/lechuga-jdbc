package org.moncheta.jdbc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class FileUtils {

    public static String loadFileFromClasspath(final String fileName, final String charSetName) {
        final InputStream is = loadFileFromClasspath(fileName);
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(is, charSetName);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return readFromReader(reader);
    }

    public static InputStream loadFileFromClasspath(final String fileName) {
        final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            throw new RuntimeException("could not open file: " + fileName);
        }
        return is;
    }

    static String readFromReader(final Reader reader) {
        final BufferedReader r = new BufferedReader(reader);
        final StringBuilder strb = new StringBuilder();
        try {
            while (true) {
                final String line = r.readLine();
                if (line == null) {
                    break;
                }
                strb.append(line).append('\n');
            }
            reader.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return strb.toString();
    }

}
