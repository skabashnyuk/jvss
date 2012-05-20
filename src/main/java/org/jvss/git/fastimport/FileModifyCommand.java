package org.jvss.git.fastimport;

import java.io.IOException;
import java.io.PrintStream;

/**
 */
public class FileModifyCommand implements FileCommand {

    private final int mode;

    private final DataRef dataRef;

    private final String path;

    public FileModifyCommand(int mode, DataRef dataRef, String path) {
        this.mode = mode;
        this.dataRef = dataRef;
        this.path = path;
    }

    @Override
    public void writeTo(PrintStream out, Options opts) throws IOException {
        out.append('M').append(SP).append(String.valueOf(mode)).append(SP);
        dataRef.writeTo(out, opts);
        out.append(SP).append(path).append(LF);
    }
}
