/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.payara.tooling.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * OS related utilities
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class OsUtils {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** System lines separator. */
    public static final String LINES_SEPARATOR
            = System.getProperty("line.separator");

    /** System file separator length. */
    public static final int FILE_SEPARATOR_LENGTH = File.separator.length();

    /** System property to retrieve OS name. */
    public static final String OS_NAME_PROPERTY = "os.name";

    /** OS name from system properties. */
    public static final String OS_NAME = System.getProperty(OS_NAME_PROPERTY);

    /** OS name from system properties converted to upper case. */
    public static final String OS_NAME_UPCASE =
            OS_NAME != null ? OS_NAME.toUpperCase() : OS_NAME;

    /**
     * Windows OS name substring used to identify Windows in OS name converted
     * to upper case.
     */
    private static final String OS_WIN_SUBSTR = "WINDOWS";
    
    /**
     * Test if OS where this JDK is running windows.
     * <p/>
     * Internally cached value.
     */
    private static final boolean IS_WIN = 
            OS_NAME != null ? OS_NAME_UPCASE.contains(OS_WIN_SUBSTR) : false;

    /** Executable file suffix (nothing on UNIX, .exe on windows, etc.). */
    public static final String  EXEC_SUFFIX = IS_WIN ? ".exe" : "";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Check if OS where this JDK is running is windows.
     * <p/>
     * @return <code>true</code> when this JDK is running on Windows
     *         or <code>false</code> otherwise.
     */
    public static boolean isWin() {
        return IS_WIN;
    }

    /**
     * Parses parameters from a given string in shell-like manner and append
     * them to executable file.
     * <p/>
     * Users of the Bourne shell (e.g. on Unix) will already be familiar with
     * the behavior. For example you should be able to:
     * <ul>
     * <li/>Include command names with embedded spaces, such as
     *     <code>c:\Program Files\jdk\bin\javac</code>.
     * <li/>Include extra command arguments, such as <code>-Dname=value</code>.
     * <li/>Do anything else which might require unusual characters
     *     or processing. For example:
     * <p/><code><pre>
     * "c:\program files\jdk\bin\java" -Dmessage="Hello /\\/\\ there!" -Xmx128m
     * </pre></code>
     * <p/>This example would create the following executable name and
     * arguments:
     * <ol>
     * <li/> <code>c:\program files\jdk\bin\java</code>
     * <li/> <code>-Dmessage=Hello /\/\ there!</code>
     * <li/> <code>-Xmx128m</code>
     * </ol>
     * Note that the command string does not escape its backslashes--under
     * the assumption that Windows users will not think to do this, meaningless
     * escapes are just left as backslashes plus following character.
     * </ul>
     * <em>Caveat</em>: even after parsing, Windows programs (such as the
     * Java launcher) may not fully honor certain characters, such as quotes,
     * in command names or arguments. This is because programs under Windows
     * frequently perform their own parsing and unescaping (since the shell
     * cannot be relied on to do this). On Unix, this problem should not occur.
     * <p/>
     * @param args A String to parse
     * @return An array of executable file and parameters to be passed to it.
     */
    public static String[] parseParameters(String exec, String args) {
        int NULL = 0x0; // STICK + whitespace or NULL + non_"
        int INPARAM = 0x1; // NULL + " or STICK + " or INPARAMPENDING + "\ // NOI18N
        int INPARAMPENDING = 0x2; // INPARAM + \
        int STICK = 0x4; // INPARAM + " or STICK + non_" // NOI18N
        int STICKPENDING = 0x8; // STICK + \
        List<String> params = new LinkedList<>();
        params.add(exec);
        char c;

        int state = NULL;
        StringBuilder buff = new StringBuilder(20);
        int slength = args.length();

        for (int i = 0; i < slength; i++) {
            c = args.charAt(i);

            if (Character.isWhitespace(c)) {
                if (state == NULL) {
                    if (buff.length() > 0) {
                        params.add(buff.toString());
                        buff.setLength(0);
                    }
                } else if (state == STICK) {
                    params.add(buff.toString());
                    buff.setLength(0);
                    state = NULL;
                } else if (state == STICKPENDING) {
                    buff.append('\\');
                    params.add(buff.toString());
                    buff.setLength(0);
                    state = NULL;
                } else if (state == INPARAMPENDING) {
                    state = INPARAM;
                    buff.append('\\');
                    buff.append(c);
                } else { // INPARAM
                    buff.append(c);
                }

                continue;
            }

            if (c == '\\') {
                if (state == NULL) {
                    ++i;

                    if (i < slength) {
                        char cc = args.charAt(i);

                        if ((cc == '"') || (cc == '\\')) {
                            buff.append(cc);
                        } else if (Character.isWhitespace(cc)) {
                            buff.append(c);
                            --i;
                        } else {
                            buff.append(c);
                            buff.append(cc);
                        }
                    } else {
                        buff.append('\\');

                        break;
                    }

                    continue;
                } else if (state == INPARAM) {
                    state = INPARAMPENDING;
                } else if (state == INPARAMPENDING) {
                    buff.append('\\');
                    state = INPARAM;
                } else if (state == STICK) {
                    state = STICKPENDING;
                } else if (state == STICKPENDING) {
                    buff.append('\\');
                    state = STICK;
                }

                continue;
            }

            if (c == '"') {
                if (state == NULL) {
                    state = INPARAM;
                } else if (state == INPARAM) {
                    state = STICK;
                } else if (state == STICK) {
                    state = INPARAM;
                } else if (state == STICKPENDING) {
                    buff.append('"');
                    state = STICK;
                } else { // INPARAMPENDING
                    buff.append('"');
                    state = INPARAM;
                }

                continue;
            }

            if (state == INPARAMPENDING) {
                buff.append('\\');
                state = INPARAM;
            } else if (state == STICKPENDING) {
                buff.append('\\');
                state = STICK;
            }

            buff.append(c);
        }

        // collect
        if (state == INPARAM) {
            params.add(buff.toString());
        } else if ((state & (INPARAMPENDING | STICKPENDING)) != 0) {
            buff.append('\\');
            params.add(buff.toString());
        } else { // NULL or STICK

            if (buff.length() != 0) {
                params.add(buff.toString());
            }
        }

        String[] retArgs = new String[params.size()];
        int i = 0;
        for (String param : params) {
            retArgs[i++] = param;
        }
        return retArgs;
    }

    /** Complementary method to parseParameters
     * @link #parseParameters
     */
    public static String escapeParameters(String[] params) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < params.length; i++) {
            escapeString(params[i], sb);
            sb.append(' ');
        }

        final int len = sb.length();

        if (len > 0) {
            sb.setLength(len - 1);
        }

        return sb.toString().trim();
    }

    /**
     * Escapes one string and appends it into provided {@link StringBuffer}.
     * <p/>
     * @param s  String to be escaped.
     * @param sb Target {@link StringBuffer}.
     * @link #escapeParameters
     */
    public static void escapeString(String s, StringBuffer sb) {
        if (s.length() == 0) {
            sb.append("\"\"");
            return;
        }
        boolean hasSpace = false;
        final int sz = sb.length();
        final int slen = s.length();
        char c;
        for (int i = 0; i < slen; i++) {
            c = s.charAt(i);
            if (Character.isWhitespace(c)) {
                hasSpace = true;
                sb.append(c);
                continue;
            }
            if (c == '\\') {
                sb.append('\\').append('\\');
                continue;
            }
            if (c == '"') {
                sb.append('\\').append('"');
                continue;
            }
            sb.append(c);
        }
        if (hasSpace) {
            sb.insert(sz, '"');
            sb.append('"');
        }
    }

    /**
     * Escapes provided {@link String}.
     * <p/>
     * @param s {@link String} to be escaped.
     * @return Escaped {@link String} value.
     */
    public static String escapeString(String s) {
        final int sLen = s.length();
        int tLen = sLen;
        boolean quote = sLen == 0;
        // Count length of target String to avoid StringBuilder resizing
        for (int i = 0; i < sLen; i++) {
            char c = s.charAt(i);
            switch(c) {
                case '\\': case '"':
                    tLen++;
                    break;
                default:
                    if (Character.isWhitespace(c)) {
                        quote = true;
                    }
            }
        }
        if (quote) {
            tLen += 2;
        }
        // Build target value.
        StringBuilder sb = new StringBuilder(tLen);
        if (quote) {
            sb.append('\"');
        }
        for (int i = 0; i < sLen; i++) {
            char c = s.charAt(i);
            switch(c) {
                case '\\': case '"':
                    sb.append('\\');
                    break;
            }
            sb.append(c);
        }
        if (quote) {
            sb.append('\"');
        }
        return sb.toString();
    }

    /**
     * Recursive delete of internal files and directory structure.
     * <p/>
     * Target directory is not removed. It's made empty.
     * Think twice before using it.
     * <p/>
     * @param target File or directory which content will be deleted.
     */
    public static boolean rmDirContent(File target) {
        boolean result = true;
        if (target != null) {
            File[] content = target.listFiles();
            for (File file : content) {
                if (file.canWrite()) {
                    if (file.isDirectory() && !".".equals(file.getName())
                            && !"..".equals(file.getName())) {
                        result = result && rmDirContent(file);
                    }
                    result = result && file.delete();
                } else {
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * Recursive delete of directory structure.
     * <p/>
     * Target directory is also removed.
     * Think twice before using it.
     * <p/>
     * @param target File or directory to be deleted.
     */
    public static boolean rmDir(File target) {
        boolean result = rmDirContent(target);
        return result && target.delete();
    }

    /**
     * Join 2 path elements separating them with path separator if not present 
     * at the end of first path element.
     * <p/>
     * @param e1 1st path element.
     * @param e2 2nd path element.
     */
    @SuppressWarnings("null")
    public static String joinPaths(final String e1, final String e2) {
        int l1 = e1 != null ? e1.length() : 0;
        int l2 = e2 != null ? e2.length() : 0;
        boolean separator = l1 > 0 && !e1.endsWith(File.separator);
        StringBuilder sb = new StringBuilder(
                l1 + l2 + (separator ? FILE_SEPARATOR_LENGTH : 0));
        if (l1 > 0) {
            sb.append(e1);
        }
        if (separator) {
            sb.append(File.separator);
        }
        if (l2 > 0) {
            sb.append(e2);
        }
        return sb.toString();
    }

}
