package org.jvss.git.fastimport;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: sj
 * Date: 20.05.12
 * Time: 18:42
 * To change this template use File | Settings | File Templates.
 */
public class Committer extends  PersonalChange{
    public Committer( Person person, Date when) {
        super("committer", person, when);
    }
}
