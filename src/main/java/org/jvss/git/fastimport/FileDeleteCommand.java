package org.jvss.git.fastimport;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Included in a commit command to remove a file or recursively delete an entire directory from the branch.
 * If the file or directory removal makes its parent directory empty,
 * the parent directory will be automatically removed too.
 * This cascades up the tree until the first non-empty directory or the root is reached.
 */
public class FileDeleteCommand implements FileCommand {

    private final String path;


    public FileDeleteCommand(String path) {
        this.path = path;
    }

    @Override
    public void writeTo(PrintStream out, Options opts) throws IOException {
        out.append('D').append(SP).append(path).append(LF);
    }
}
