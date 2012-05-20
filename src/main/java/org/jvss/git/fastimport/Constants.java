package org.jvss.git.fastimport;

import java.text.DateFormat;

/**
 *
 */
public interface Constants {
    /**
     * fast-import is very strict about its input. Where we say SP below we mean exactly one space.
     * Likewise LF means one (and only one) linefeed. Supplying additional whitespace characters will cause unexpected
     * results, such as branch names or file names with leading or trailing spaces in their name, or early termination
     * of fast-import when it encounters unexpected input.
     */
    public static final char SP = ' ';
    public static final char LF = '\n';
    /**
     * LT and GT are the literal less-than (\x3c) and greater-than (\x3e) symbols.
     * These are required to delimit the email address from the other fields in the line.
     */
    public static final char LT='<';
    public static final char GT='>';

}
