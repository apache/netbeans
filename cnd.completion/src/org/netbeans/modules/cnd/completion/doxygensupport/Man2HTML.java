/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.doxygensupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 *
 * Simple man output to HTML formatter.
 */
public class Man2HTML {
    public static final int MAX_WIDTH = 65;

    private static final Pattern ESC_PATTERN = Pattern.compile("\u001B\\[[0-9;]*m"); // NOI18N

    private enum MODE {

        NORMAL, BOLD, ITALIC;
    };
    private BufferedReader br;
    private MODE mode = MODE.NORMAL;
    private String previousLine = ""; // Buffer line

    /**
     * Simple man output to HTML formatter. Takes the output of the man command as input.
     * @param br Charater input stream
     */
    public Man2HTML(BufferedReader br) {
        this.br = br;
    }

    private void startNormal(StringBuffer buf) {
        if (mode != MODE.NORMAL) {
            if (mode == MODE.BOLD) {
                buf.append("</B>"); // NOI18N
            } else if (mode == MODE.ITALIC) {
                buf.append("</I>"); // NOI18N
            }
            mode = MODE.NORMAL;
        }
    }

    private void startBold(StringBuffer buf) {
        buf.append("<B>"); // NOI18N
        mode = MODE.BOLD;
    }

    private void startItalic(StringBuffer buf) {
        buf.append("<I>"); // NOI18N
        mode = MODE.ITALIC;
    }


    private int countIndent(String line) {
        int indent = 0;
        while (indent < line.length() && line.charAt(indent) == ' ') {
            indent++;
        }
        return indent;
    }

    private int breakAtColumn(String line) {
        int column = 0;
        int breakAt = 0;
        while (breakAt < line.length() && column <= MAX_WIDTH+1) {
            char ch = line.charAt(breakAt);
            if (ch == '\b') {
                column--;
            } else if (ch == ' ') {
                column++;
            } else {
                column++;
            }
            breakAt++;
        }
        if (column >= (MAX_WIDTH+1)) {
            while (--breakAt > 0) {
                char ch = line.charAt(breakAt);
                if (ch == ' ') {
                    break;
                }
            }
            if (breakAt > 0) {
                return breakAt;
            }
        }
        return -1;
    }

    private String getLine() {
        String line = null;
        try {
            line = br.readLine();
        } catch (IOException ioe) {
        }
        if (line != null) {
            line = stripTerminalEscapes(line);
        }
        return line;
    }

    /**
     * Strips escape sequences that set display attributes (font color
     * and other decoration). Such sequences start with ESC (\u001B)
     * followed by '[' and end with 'm'. They are found in <code>man</code>
     * output (see bug #183176), and it's not trivial to tell <code>man</code>
     * not to add them. So filter them out here.
     *
     * Unrecognized escape sequences are not stripped to prevent accidental
     * damage of the text.
     *
     * @param line  line to strip escape sequences from
     * @return line after stripping
     */
    private String stripTerminalEscapes(String line) {
        return ESC_PATTERN.matcher(line).replaceAll(""); // NOI18N
    }

    private String getNextLine() {
        String line = null;
        while (line == null) {
            line = getLine();
            if (line == null) {
                // end of file
                break;
            }
            // Skip headers/footers
            if (line.contains("BSD ") || // NOI18N
                line.startsWith("Standard C") || // NOI18N
                line.startsWith("SunOS") || // NOI18N
                line.startsWith("User Commands")) { // NOI18N
                line = null;
            }
            // Skip multiple blank lines
            if (previousLine != null && previousLine.length() == 0 && line != null && line.length() == 0) {
                line = null;
            }
            if (line != null && (line.startsWith("    |") || line.startsWith("     __"))) { // NOI18N
                line = line.substring(4);
            }
//            if (line != null && line.startsWith("     ")) { // NOI18N
//                line = line.substring(4);
//            }
            // Break at MAX_COLUMN;
            if (line != null) {
                int i = breakAtColumn(line);
                if (i > 0) {
                    line = line.substring(0, i) + "\n" + line.substring(0, countIndent(line)) + line.substring(i+1); // NOI18N
                    int j = 0;
                }
            }

        }
        previousLine = line;
        return line;
    }

    /**
     * Run the formatter.
     * @return the formattet html document as a String
     */
    public String getHTML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<HTML>\n"); // NOI18N
        buf.append("<BODY>\n"); // NOI18N
        buf.append("<PRE>\n"); // NOI18N
        buf.append("<FONT SIZE=\"3\">\n"); // NOI18N

        char prevCh = 0;
        char curCh = 0;
        char nextCh = 0;

        int curColumn = 0;

        String line = null;
        while ((line = getNextLine()) != null) {
            for (int i = 0; i < line.length(); i++) {
                prevCh = curCh;
                curCh = nextCh;
                nextCh = line.charAt(i);

                if (nextCh == '\b') {
                    if (mode == MODE.NORMAL) {
                        if (curCh == '_') {
                            startItalic(buf);
                        } else {
                            startBold(buf);
                        }
                    }
                } else {
                    if (curCh != 0 && curCh != '\b') {
                        if (prevCh != 0 && prevCh != '\b') {
                            startNormal(buf);
                        }
                        // Just append the char to line. Escape if necessary.
                        if (curCh == '<') {
                            buf.append("&lt;"); // NOI18N
                        } else if (curCh == '>') {
                            buf.append("&gt;"); // NOI18N
                        } else if (curCh == '\"') {
                            buf.append("&rdquo;"); // NOI18N
                        } else if (curCh == '\'') {
                            buf.append("&rsquo;"); // NOI18N
                        } else if (curCh == '`') {
                            buf.append("&lsquo;"); // NOI18N
                        } else if (curCh == '&') {
                            buf.append("&amp;"); // NOI18N
                        } else {
                            buf.append(curCh);
                        }
                        curColumn++;
//                        if (curColumn >= MAX_WIDTH) {
//                            break;
//                        }
                    }
                }
            }


            if (nextCh != 0) {
                buf.append(nextCh);
            }
            startNormal(buf);
            prevCh = 0;
            curCh = 0;
            nextCh = 0;
            curColumn = 0;
            buf.append("\n"); // NOI18N
        }

//        buf.append("</small>\n"); // NOI18N
        buf.append("</FONT>\n"); // NOI18N
        buf.append("</PRE>\n"); // NOI18N
        buf.append("</BODY>\n"); // NOI18N
        buf.append("</HTML>\n"); // NOI18N
        return buf.toString();
    }
}
