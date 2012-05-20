package org.jvss.git.fastimport;

import java.io.PrintStream;

/**
 */
public interface Command {
    /**
     * fast-import is very strict about its input. Where we say SP below we mean exactly one space.
     * Likewise LF means one (and only one) linefeed. Supplying additional whitespace characters will cause unexpected
     * results, such as branch names or file names with leading or trailing spaces in their name, or early termination
     * of fast-import when it encounters unexpected input.
     */
    public static final char SP = ' ';
    public static final char LF = '\n';
    /**
     * Commands to update the current repository and control the current import process. More detailed discussion (with examples) of each command follows later.
     */
    public enum Type {
        commit,//Creates a new branch or updates an existing branch by creating a new commit and updating the branch to point at the newly created commit.
        tag,//Creates an annotated tag object from an existing commit or branch. Lightweight tags are not supported by this command, as they are not recommended for recording meaningful points in time.
        reset,// Reset an existing branch (or a new branch) to a specific revision. This command must be used to change a branch to a specific revision without making a commit on it.
        blob, //Convert raw file data into a blob, for future use in a commit command. This command is optional and is not needed to perform an import.
        checkpoint,// Forces fast-import to close the current packfile, generate its unique SHA-1 checksum and index, and start a new packfile. This command is optional and is not needed to perform an import.
        progress, //Causes fast-import to echo the entire line to its own standard output. This command is optional and is not needed to perform an import.
        feature, //Require that fast-import supports the specified feature, or abort if it does not.
        option// Specify any of the options listed under OPTIONS that do not change stream semantic to suit the frontend's needs. This command is optional and is not needed to perform an import.
        ;

    }

    Type getType();

    /**
     * Write content of the command to output stream.
     *
     * @param out
     */
    void writeTo(PrintStream out);
}
