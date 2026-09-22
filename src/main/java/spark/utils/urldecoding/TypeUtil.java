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
 * Hex-digit and hex-string conversion utilities, used by this package's URL percent-encoding
 * decoder to parse {@code %XX} escapes and by {@link Utf8Appendable} to render bytes for
 * diagnostic messages.
 * <p>
 * Copied from Eclipse Jetty's own {@code org.eclipse.jetty.util.TypeUtil}, trimmed down to just
 * the methods this package actually uses - the original is a much larger, general-purpose type
 * utility class.
 *
 * @since Jetty 4.1
 */
public class TypeUtil {

    private TypeUtil() {
    }

    /**
     * Parse an int from a substring, using the specified base (radix).
     * Negative numbers are not handled.
     * <p>
     * For example, {@code parseInt("id=ff", 3, 2, 16)} parses the two characters at index 3
     * ({@code "ff"}) as base-16, returning {@code 255}.
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
     * Converts a single ASCII hex digit character to its numeric value.
     * For example, {@code convertHexDigit('a')} returns {@code 10}.
     *
     * @param c An ASCII encoded character ({@code 0-9}, {@code a-f}, {@code A-F})
     * @return The integer value of the hex digit, in the range 0-15.
     * @throws NumberFormatException if c is not a valid hex digit character
     */
    public static int convertHexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        throw new NumberFormatException("'" + c + "' is not a valid hex digit");
    }

    /**
     * Same as {@link #convertHexDigit(char)}, but takes the character as an int code point
     * rather than a char.
     * For example, {@code convertHexDigit((int) 'a')} returns {@code 10}.
     *
     * @param c An ASCII encoded character ({@code 0-9}, {@code a-f}, {@code A-F})
     * @return The integer value of the hex digit, in the range 0-15.
     * @throws NumberFormatException if c is not a valid hex digit character
     */
    public static int convertHexDigit(int c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        throw new NumberFormatException("'" + c + "' is not a valid hex digit");
    }

    /**
     * Convert a byte to its 2-character hex string representation.
     * <p>
     * The high nibble is rendered uppercase and the low nibble lowercase - for example,
     * {@code toHexString((byte) 0xAB)} returns {@code "Ab"}, not {@code "AB"} or {@code "ab"}.
     * This has been the behavior since this class was introduced and is documented here
     * rather than changed.
     *
     * @param b the byte to convert
     * @return the 2-character hex string representation of b
     */
    public static String toHexString(byte b) {
        return toHexString(new byte[] {b}, 0, 1);
    }

    /**
     * Convert a range of a byte array to its hex string representation, two characters per
     * byte. See {@link #toHexString(byte)} for the per-byte character casing.
     * <p>
     * For example, {@code toHexString(new byte[] {(byte) 0xDE, (byte) 0xAD}, 0, 2)} returns
     * {@code "DeAd"}.
     *
     * @param b      the byte array to convert
     * @param offset offset of the first byte to convert
     * @param length number of bytes to convert
     * @return the hex string representation of the specified range of b
     */
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
