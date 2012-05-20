package org.jvss.git.fastimport;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Recursively copies an existing file or subdirectory to a different location within the branch.
 * The existing file or directory must exist.
 * If the destination exists it will be completely replaced by the content copied from the source.
 */
public class FileCopyCommand implements FileCommand {

    private final String sourceLocation;

    private final String destinationLocation;

    public FileCopyCommand(String sourceLocation, String destinationLocation) {
        this.sourceLocation = sourceLocation;
        this.destinationLocation = destinationLocation;
    }


    @Override
    public void writeTo(PrintStream out, Options opts) throws IOException {
        out.append('C').append(SP).append(sourceLocation).append(SP).append(destinationLocation).append(LF);
    }
}
