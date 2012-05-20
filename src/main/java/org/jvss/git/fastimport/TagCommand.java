package org.jvss.git.fastimport;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Creates an annotated tag referring to a specific commit.
 */
public class TagCommand implements Writable, Constants {

    private final String name;

    private final Tagger tagger;

    private final Message message;

    public TagCommand(String name, Tagger tagger, Message message) {
        this.name = name;
        this.tagger = tagger;
        this.message = message;
    }

    @Override
    public void writeTo(PrintStream out, Options opts) throws IOException {
        out.append("tag").append(SP).append(name).append(LF);
        tagger.writeTo(out, opts);
        message.writeTo(out, opts);
    }
}
