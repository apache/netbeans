/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

// @todo The current version does not handle comment tokens inside "" or ''
//       correct! (remember that such a section may span multiple lines!!!)

package org.netbeans.modules.tasklist.todo;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The Source Code Comment Parser allows you to read the comments in a source
 * code line by line.
 *
 * @author Trond Norbye
 * @author S. Aubrecht
 */
final class SourceCodeCommentParser {

    /**
     * Default instance, treat all lines as comments!!
     */
    public SourceCodeCommentParser() {
        parser = new SourceParser();
    }
    
    /**
     * Create a new instance of SourceCodeCommentParser that supports single-
     * line comments, and multiline comments
     * @param lineComment the start tag for a single-line comment
     * @param blockStart the start tag of a block comment
     * @param blockEnd the end tag of a block comment
     */
    public SourceCodeCommentParser(String lineComment, 
                                   String blockStart, 
                                   String blockEnd) {
        parser = new CommentParser(lineComment, blockStart, blockEnd);
    }
    
    /**
     * Set the document to parse
     * @param doc the document to parse
     */
    public void setText( String text ) {
        parser.setText( text );
    }
    
    /**
     * get the range for the next comment line...
     * @param ret Where to store the result
     * @return false when EOF, true otherwise
     */
    public boolean nextRegion(CommentRegion reg) throws IOException {
        return parser.nextRegion(reg);
    }

    /**
     * I don't know if this was a smart thing to do, but instead of testing
     * each time if I should skip comments or not, I decided to create an
     * an internal parser that I could extend to my needs... The most generic
     * parser treats everything as comments, and should hence "work" for all
     * unknown file types ;)
     */
    private static class SourceParser {
        
        /**
         * Create a new instance of the SourceParser
         */
        public SourceParser() {
            text = null;
            curr = 0;
            matcher = null;
        }
        
        /**
         * Get the indexes of the next comment region..
         * @param ret Where to store the result
         * @return false when EOF, true otherwise
         * @throws java.io.IOException if a read error occurs on the input
         *         stream.
         */
        public boolean nextRegion(CommentRegion reg) throws IOException {
            if (text == null) {
                return false;
            }

            reg.start = curr;
            reg.stop = text.length();

            if (reg.start == reg.stop) {
                return false;
            }

            curr = reg.stop;
            return true;
        }
                
        /**
         * Set the document to parse
         * @param doc the document to parse
         */
        public void setText( String text ) {
            this.text = text;

            if (pattern != null) {
                matcher = pattern.matcher(text);
            }
        }

        /**
         * Append all characters in a string to a stringbuffer as \\unnnn
         * @param buf destination buffer
         * @param str the string to append
         */
        protected void appendEncodedChars(StringBuffer buf, String str) {
            int len = str.length();
            
            for (int ii = 0; ii < len; ++ii) {
                String s = Integer.toHexString((int)str.charAt(ii));
                
                buf.append("\\u");
                for(int i = 0, n = 4 - s.length(); i < n; i++) {
                    buf.append('0');
                }
                buf.append(s);
            }
        }

        /**
         * A StringBuffer that I use towards the source reader to avoid the
         * creation of a lot of strings...
         */
        protected String text;
        
        /** current position in the text*/
        protected int curr;

        /** A matcher that may be utilized by a subclass... */
        protected Matcher matcher;
        /** The pattern to search for in the text */
        protected Pattern pattern;

    }
    
    /**
     * The comment parser exstend the source parser with functionality to
     * create single line comments, and a block of lines that are treated as
     * a comment.
     */
    private static class CommentParser extends SourceParser {
        /**
         * Create a new instance of the comment parser that only supports
         * a "single-line" comments
         * @param lineComment the token to start a line comment
         */
        public CommentParser(String lineComment) {
            this(lineComment, null, null);
        }
        
        /**
         * Create a new instance of the comment parser that supports:
         * @param lineComment the token for a single line comment
         * @param blockStart the start token for a multiline comment block
         * @param blockEnd the end token for a multiline comment block
         */
        public CommentParser(String lineComment,
                             String blockStart,
                             String blockEnd) {
            super();
            this.lineComment = lineComment;
            this.blockStart = blockStart;
            this.blockEnd = blockEnd;

            StringBuffer sb = new StringBuffer();
            
            boolean needor = false;

            if (lineComment != null) {
                appendEncodedChars(sb, lineComment);
                needor = true;
            }

            if (blockStart != null && !blockStart.isEmpty()) {
                if (needor) {
                    sb.append('|');
                }
                appendEncodedChars(sb, blockStart);
            }

            pattern = Pattern.compile(sb.toString());
            matcher = null;
        }

        /**
         * Get the next line of text from the file.
         * @param reg Where to store the result
         * @return false when EOF, true otherwise
         * @throws java.io.IOException if a read error occurs on the input
         *         stream.
         */
        public boolean nextRegion(CommentRegion reg) throws IOException {
            boolean ret = false;
            
            if (matcher != null && matcher.find(curr)) {
                String token = text.substring(matcher.start(), matcher.end());

                reg.start = matcher.start();

                if (!lineComment.isEmpty() && lineComment.equals(token)) {
                    int idx = text.indexOf("\n", reg.start);
                    if (idx != -1) {
                        reg.stop = idx;
                    } else {
                        reg.stop = text.length();
                    }
                } else if (!blockStart.isEmpty()) {
                    int idx = text.indexOf(blockEnd, reg.start);
                    if (idx != -1) {
                        reg.stop = idx + blockEnd.length();
                    } else {
                        reg.stop = text.length();
                    }
                } else {
                    return false;  // no need to scan for commens if these are not defined at all
                }

                curr = reg.stop + 1;
                ret = true;
            }
            return ret;
        }
        
        /** The string that indicates the start of a single line comment */
        protected String  lineComment;
        /** The string that indicates the start of a multiline comment */
        protected String  blockStart;
        /** The string that indicates the end of a multiline comment */
        protected String  blockEnd;

    }

    /** A little handy struct to pass up to the parent.. */
    static class CommentRegion {
        /** The position in the text where the comment starts */
        public int start;
        /** The position in the text where the comment ends */
        public int stop; 
        
        /** Create a new instance */
        public CommentRegion() {
            start = stop = 0;
        }
    }

    /** The parser used by this SourceCodeCommentParser */
    private SourceParser parser;
}
