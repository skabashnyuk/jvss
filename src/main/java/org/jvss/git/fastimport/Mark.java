package org.jvss.git.fastimport;

import java.io.IOException;
import java.io.PrintStream;

/**
 */
public class Mark extends DataRef {
    /**
     * Arranges for fast-import to save a reference to the current object,
     * allowing the frontend to recall this object at a future point in time, without knowing its SHA-1.
     */
    private final int idNum;

    private static int currentMark = 1;

    public Mark(int idNum) {
        this.idNum = idNum;
    }

    @Override
    public void writeTo(PrintStream out, Options opts) throws IOException {
        out.append("mark").append(SP).append(':').append(String.valueOf(idNum)).append(LF);
    }

    public static Mark nextMark() {
        return new Mark(currentMark++);
    }
}
