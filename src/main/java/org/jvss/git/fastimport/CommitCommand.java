package org.jvss.git.fastimport;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Create or update a branch with a new commit, recording one logical change to the project.
 * 'commit' SP <ref> LF
 * mark?
 * ('author' (SP <name>)? SP LT <email> GT SP <when> LF)?
 * 'committer' (SP <name>)? SP LT <email> GT SP <when> LF
 * data
 * ('from' SP <committish> LF)?
 * ('merge' SP <committish> LF)?
 * (filemodify | filedelete | filecopy | filerename | filedeleteall | notemodify)*
 * LF?
 * where <ref> is the name of the branch to make the commit on. Typically branch names are prefixed with refs/heads/ in Git, so importing the CVS branch symbol RELENG-1_0 would use refs/heads/RELENG-1_0 for the value of <ref>. The value of <ref> must be a valid refname in Git. As LF is not valid in a Git refname, no quoting or escaping syntax is supported here.
 * A mark command may optionally appear, requesting fast-import to save a reference to the newly created commit for future use by the frontend (see below for format). It is very common for frontends to mark every commit they create, thereby allowing future branch creation from any imported commit.
 * <p/>
 * The data command following committer must supply the commit message (see below for data command syntax). To import an empty commit message use a 0 length data. Commit messages are free-form and are not interpreted by Git. Currently they must be encoded in UTF-8, as fast-import does not permit other encodings to be specified.
 * <p/>
 * Zero or more filemodify, filedelete, filecopy, filerename, filedeleteall and notemodify commands may be included to update the contents of the branch prior to creating the commit. These commands may be supplied in any order. However it is recommended that a filedeleteall command precede all filemodify, filecopy, filerename and notemodify commands in the same commit, as filedeleteall wipes the branch clean (see below).
 * <p/>
 * The LF after the command is optional (it used to be required).
 */
public class CommitCommand implements Writable, Constants {
    /**
     * Name of the branch to make the commit on
     */
    private final String ref;
    /**
     * An author command may optionally appear, if the author information might differ from the committer information.
     */
    private final Author author;
    /**
     * The committer command indicates who made this commit, and when they made it.
     */
    private final Committer committer;

    private final Message commitMessage;
    /**
     * The from command is used to specify the commit to initialize this branch from. This revision will be the first
     * ancestor of the new commit.
     */
    private final String from;

    public CommitCommand(String ref, Author author, Committer committer, Message commitMessage, String from) {
        this.ref = ref;
        this.author = author;
        this.committer = committer;
        this.commitMessage = commitMessage;
        this.from = from;
    }

    public CommitCommand(String ref, Committer committer, Message commitMessage, String from) {
        this(ref, null, committer, commitMessage, from);
    }

    @Override
    public void writeTo(PrintStream out, Options opts) throws IOException {
        out.append("commit").append(SP).append(ref).append(LF);
        //mark???
        if (author != null) {
            author.writeTo(out, opts);
        }

        committer.writeTo(out, opts);
        commitMessage.writeTo(out,opts);


    }
}
