package org.jvss.git.fastimport;

import java.io.PrintStream;

/**
 * Here <name> is the person's display name (for example "Com M Itter")
 * and <email> is the person's email address ("cm@example.com [1] ")
 */
public class Person implements Writable, Constants {
    private final String name;

    private final String email;

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }


    @Override
    public void writeTo(PrintStream out, Options opts) {
        out.append(SP).append(name).append(SP).append(LF).append(email).append(SP);
    }
}
