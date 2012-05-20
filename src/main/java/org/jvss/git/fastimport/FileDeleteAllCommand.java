package org.jvss.git.fastimport;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Included in a commit command to remove all files (and also all directories) from the branch.
 * This command resets the internal branch structure to have no files in it, allowing the frontend
 * to subsequently add all interesting files from scratch.
 */
public class FileDeleteAllCommand implements FileCommand{

    @Override
    public void writeTo(PrintStream out, Options opts) throws IOException {
        out.append("deleteall").append(LF);
    }
}
