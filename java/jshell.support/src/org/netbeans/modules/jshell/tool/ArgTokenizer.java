/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.jshell.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toList;

/**
 * Parse command arguments, derived from StreamTokenizer by
 * @author  James Gosling
 */
class ArgTokenizer {

    private final String str;
    private final String prefix;
    private final int length;
    private int next = 0;
    private char buf[] = new char[20];
    private int mark;

    private final byte ctype[] = new byte[256];
    private static final byte CT_ALPHA = 0;
    private static final byte CT_WHITESPACE = 1;
    private static final byte CT_QUOTE = 8;

    private String sval;
    private boolean isQuoted = false;

    private final Map<String, Boolean> options = new HashMap<>();
    private final List<String> badOptions = new ArrayList<>();

    ArgTokenizer(String prefix, String arg) {
        this.str = arg;
        this.prefix = prefix + " ";
        this.length = arg.length();
        quoteChar('"');
        quoteChar('\'');
        whitespaceChars(0x09, 0x0D);
        whitespaceChars(0x1C, 0x20);
        whitespaceChars(0x85, 0x85);
        whitespaceChars(0xA0, 0xA0);
    }

    /**
     * Return the next non-option argument. Encountered options are stored.
     *
     * @return the token string, or null if there are no more tokens
     */
    String next() {
        while (true) {
            nextToken();
            if (sval != null && !isQuoted() && sval.startsWith("-")) {
                // allow POSIX getopt() option format,
                // to be consistent with command-line
                String opt = sval.startsWith("--")
                        ? sval.substring(1)
                        : sval;
                foundOption(opt);
            } else {
                break;
            }
        }
        return sval;
    }

    private void foundOption(String opt) {
        if (options.containsKey(opt)) {
            options.put(opt, true);
            return;
        }

        List<Map.Entry<String,Boolean>> matches =
                options.entrySet()
                       .stream()
                       .filter(e -> e.getKey().startsWith(opt))
                       .collect(toList());
        if (matches.size() == 1) {
            matches.get(0).setValue(true);
        } else {
            badOptions.add(opt);
        }
    }

    /**
     * Set the allowed options. Must be called before any options would be read
     * and before calling any of the option functionality below.
     */
    void allowedOptions(String... opts) {
        for (String opt : opts) {
            options.putIfAbsent(opt, false);
        }
    }

    /**
     * Has the specified option been encountered.
     *
     * @param opt the option to check
     * @return true if the option has been encountered
     */
    boolean hasOption(String opt) {
        Boolean has = options.get(opt);
        if (has == null) {
            throw new InternalError("hasOption called before allowedOptions or on bad option");
        }
        return has;
    }

    /**
     * Return the number of encountered options
     *
     * @return the option count
     */
    int optionCount() {
        return (int) options.entrySet().stream()
                .filter(e -> e.getValue())
                .count();
    }

    /**
     * Return the bad options encountered. Bad options are those that were not
     * listed in the call to allowedOptions().
     *
     * @return as space-separated list the bad options encountered, or the empty
     * string if none.
     */
    String badOptions() {
        return String.join(" ", badOptions);
    }

    /**
     * Consume the remainder of the input. This is useful to sure all options
     * have been encountered and to check to unexpected additional non-option
     * input.
     *
     * @return the string-separated concatenation of all remaining non-option
     * arguments.
     */
    String remainder() {
        List<String> rem = new ArrayList<>();
        while (next() != null) {
            rem.add(sval);
        }
        return String.join(" ", rem);
    }

    String val() {
        return sval;
    }

    boolean isQuoted() {
        return isQuoted;
    }

    String whole() {
        return prefix + str;
    }

    void mark() {
        mark = next;
    }

    void rewind() {
        next = mark;
    }

    /**
     * Reads a single character.
     *
     * @return The character read, or -1 if the end of the stream has been
     * reached
     */
    private int read() {
        if (next >= length) {
            return -1;
        }
        return str.charAt(next++);
    }

    /**
     * Specifies that all characters <i>c</i> in the range
     * <code>low&nbsp;&lt;=&nbsp;<i>c</i>&nbsp;&lt;=&nbsp;high</code>
     * are white space characters. White space characters serve only to
     * separate tokens in the input stream.
     *
     * <p>Any other attribute settings for the characters in the specified
     * range are cleared.
     *
     * @param   low   the low end of the range.
     * @param   hi    the high end of the range.
     */
    private void whitespaceChars(int low, int hi) {
        if (low < 0)
            low = 0;
        if (hi >= ctype.length)
            hi = ctype.length - 1;
        while (low <= hi)
            ctype[low++] = CT_WHITESPACE;
    }

    /**
     * Specifies that matching pairs of this character delimit string
     * constants in this tokenizer.
     * <p>
     * If a string quote character is encountered, then a string is
     * recognized, consisting of all characters after (but not including)
     * the string quote character, up to (but not including) the next
     * occurrence of that same string quote character, or a line
     * terminator, or end of file. The usual escape sequences such as
     * {@code "\u005Cn"} and {@code "\u005Ct"} are recognized and
     * converted to single characters as the string is parsed.
     *
     * <p>Any other attribute settings for the specified character are cleared.
     *
     * @param   ch   the character.
     */
    private void quoteChar(int ch) {
        if (ch >= 0 && ch < ctype.length)
            ctype[ch] = CT_QUOTE;
    }

    private int unicode2ctype(int c) {
        switch (c) {
            case 0x1680:
            case 0x180E:
            case 0x200A:
            case 0x202F:
            case 0x205F:
            case 0x3000:
                return CT_WHITESPACE;
            default:
                return CT_ALPHA;
        }
    }

    /**
     * Parses the next token of this tokenizer.
     */
    public void nextToken() {
        byte ct[] = ctype;
        int c;
        int lctype;
        sval = null;
        isQuoted = false;

        do {
            c = read();
            if (c < 0) {
                return;
            }
            lctype = (c < 256) ? ct[c] : unicode2ctype(c);
        } while (lctype == CT_WHITESPACE);

        if (lctype == CT_ALPHA) {
            int i = 0;
            do {
                if (i >= buf.length) {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                }
                buf[i++] = (char) c;
                c = read();
                lctype = c < 0 ? CT_WHITESPACE : (c < 256)? ct[c] : unicode2ctype(c);
            } while (lctype == CT_ALPHA);
            if (c >= 0) --next; // push last back
            sval = String.copyValueOf(buf, 0, i);
            return;
        }

        if (lctype == CT_QUOTE) {
            int quote = c;
            int i = 0;
            /* Invariants (because \Octal needs a lookahead):
             *   (i)  c contains char value
             *   (ii) d contains the lookahead
             */
            int d = read();
            while (d >= 0 && d != quote) {
                if (d == '\\') {
                    c = read();
                    int first = c;   /* To allow \377, but not \477 */
                    if (c >= '0' && c <= '7') {
                        c = c - '0';
                        int c2 = read();
                        if ('0' <= c2 && c2 <= '7') {
                            c = (c << 3) + (c2 - '0');
                            c2 = read();
                            if ('0' <= c2 && c2 <= '7' && first <= '3') {
                                c = (c << 3) + (c2 - '0');
                                d = read();
                            } else
                                d = c2;
                        } else
                          d = c2;
                    } else {
                        switch (c) {
                        case 'a':
                            c = 0x7;
                            break;
                        case 'b':
                            c = '\b';
                            break;
                        case 'f':
                            c = 0xC;
                            break;
                        case 'n':
                            c = '\n';
                            break;
                        case 'r':
                            c = '\r';
                            break;
                        case 't':
                            c = '\t';
                            break;
                        case 'v':
                            c = 0xB;
                            break;
                        }
                        d = read();
                    }
                } else {
                    c = d;
                    d = read();
                }
                if (i >= buf.length) {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                }
                buf[i++] = (char)c;
            }

            if (d == quote) {
                isQuoted = true;
            }
            sval = String.copyValueOf(buf, 0, i);
        }
    }
}
