package org.jvss.git.fastimport;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Indicates what object can be written to stream.
 */
public interface Writable {

    /**
     * Write content to output stream.
     *
     * @param out
     */
    void writeTo(PrintStream out, Options opts) throws IOException;
}
