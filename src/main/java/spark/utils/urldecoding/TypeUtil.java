//
//  ========================================================================
//  Copyright (c) 1995-2015 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//
package spark.utils.urldecoding;

import java.util.Objects;

/**
 * TYPE Utilities.
 * Provides various static utility methods for manipulating types and their
 * string representations.
 *
 * @since Jetty 4.1
 */
public class TypeUtil {

    private TypeUtil() {
    }

    /**
     * Parse an int from a substring.
     * Negative numbers are not handled.
     *
     * @param s      String
     * @param offset Offset within string
     * @param length Length of integer or -1 for the remainder of string
     * @param base   base of the integer
     * @return the parsed integer
     * @throws NumberFormatException if the string cannot be parsed
     * @throws NullPointerException if s is null
     * @throws IllegalArgumentException if offset or length is out of bounds for s
     */
    public static int parseInt(String s, int offset, int length, int base)
        throws NumberFormatException {
        Objects.requireNonNull(s, "s must not be null");
        if (offset < 0 || offset > s.length()) {
            throw new IllegalArgumentException("offset " + offset + " is out of bounds for a string of length " + s.length());
        }

        int value = 0;

        if (length < 0) {
            length = s.length() - offset;
        }

        if (offset + length > s.length()) {
            throw new IllegalArgumentException("offset " + offset + " + length " + length
                    + " exceeds string length " + s.length());
        }

        for (int i = 0; i < length; i++) {
            char c = s.charAt(offset + i);

            int digit = convertHexDigit(c);
            if (digit < 0 || digit >= base) {
                throw new NumberFormatException("'" + c + "' at index " + (offset + i)
                        + " is not a valid base-" + base + " digit (in \"" + s + "\")");
            }
            value = value * base + digit;
        }
        return value;
    }

    /**
     * @param c An ASCII encoded character ({@code 0-9}, {@code a-f}, {@code A-F})
     * @return The integer value of the hex digit, in the range 0-15.
     */
    public static int convertHexDigit(char c) {
        int d = Character.digit(c, 16);
        if (d < 0) {
            throw new NumberFormatException("'" + c + "' is not a valid hex digit");
        }
        return d;
    }

    /**
     * @param c An ASCII encoded character ({@code 0-9}, {@code a-f}, {@code A-F})
     * @return The integer value of the hex digit, in the range 0-15.
     */
    public static int convertHexDigit(int c) {
        int d = Character.digit(c, 16);
        if (d < 0) {
            throw new NumberFormatException("'" + c + "' is not a valid hex digit");
        }
        return d;
    }

    public static String toHexString(byte b) {
        return toHexString(new byte[] {b}, 0, 1);
    }

    public static String toHexString(byte[] b, int offset, int length) {
        var builder = new StringBuilder();
        for (int i = offset; i < offset + length; i++) {
            int bi = 0xff & b[i];
            int c = '0' + (bi / 16) % 16;
            if (c > '9') {
                c = 'A' + (c - '0' - 10);
            }
            builder.append((char) c);
            c = '0' + bi % 16;
            if (c > '9') {
                c = 'a' + (c - '0' - 10);
            }
            builder.append((char) c);
        }
        return builder.toString();
    }
}
