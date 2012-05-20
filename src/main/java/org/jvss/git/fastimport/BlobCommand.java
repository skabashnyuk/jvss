package org.jvss.git.fastimport;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Requests writing one file revision to the packfile.
 * The revision is not connected to any commit;
 * this connection must be formed in a subsequent commit command by referencing the blob through an assigned mark.
 */
public class BlobCommand implements Writable, Constants{
    private  final Mark mark;

    private final Data data;

    public BlobCommand(Mark mark, Data data) {
        this.mark = mark;
        this.data = data;
    }
    public BlobCommand(Data data) {
        this(Mark.nextMark(), data);
    }

    @Override
    public void writeTo(PrintStream out, Options opts) throws IOException {
        out.append("blob").append(LF);
        mark.writeTo(out, opts);
        data.writeTo(out, opts);
    }
}
