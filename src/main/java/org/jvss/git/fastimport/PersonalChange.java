package org.jvss.git.fastimport;

import java.io.PrintStream;
import java.util.Date;

/**
 *
 */
public class PersonalChange implements Writable, Constants {
    private final String role;

    private final Person person;

    private final Date when;

    public PersonalChange(String role, Person person, Date when) {
        this.role = role;
        this.person = person;
        this.when = when;
    }

    @Override
    public void writeTo(PrintStream out, Options opts) {
        out.append(role);
        person.writeTo(out, opts);
        out.append(SP);
        out.append(Long.toString(when.getTime()));
        out.append(LF);
    }

    public String getRole() {
        return role;
    }

    public Person getPerson() {
        return person;
    }

    public Date getWhen() {
        return when;
    }
}
