package org.jvss.git.fastimport;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Supplies raw data (for use as blob/file content, commit messages, or annotated tag messages) to fast-import.
 * Data can be supplied using an exact byte count or delimited with a terminating line.
 */
public class Data implements Writable, Constants {
    private final byte[] data;

    public Data(byte[] data) {
        this.data = data;
    }

    @Override
    public void writeTo(PrintStream out, Options opts) throws IOException {
        out.append("data").append(SP).append(String.valueOf(data.length)).append(LF);
        out.write(data);
    }
}
