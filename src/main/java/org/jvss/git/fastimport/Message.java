package org.jvss.git.fastimport;

/**
 * Created with IntelliJ IDEA.
 * User: sj
 * Date: 20.05.12
 * Time: 19:19
 * To change this template use File | Settings | File Templates.
 */
public class Message extends Data {
    public Message(String message) {
        super(message.getBytes());
    }
}
