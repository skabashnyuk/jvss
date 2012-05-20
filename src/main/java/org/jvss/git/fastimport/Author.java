package org.jvss.git.fastimport;

import java.util.Date;

/**
 */
public class Author extends PersonalChange {

    public Author(Person person, Date when) {
        super("author", person, when);
    }
}
