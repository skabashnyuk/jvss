package org.jvss.git.fastimport;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: sj
 * Date: 20.05.12
 * Time: 22:52
 * To change this template use File | Settings | File Templates.
 */
public class Tagger extends PersonalChange {

    public Tagger(Person person, Date when) {
        super("tagger", person, when);
    }
}
