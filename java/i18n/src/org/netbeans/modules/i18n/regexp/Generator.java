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


package org.netbeans.modules.i18n.regexp;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generator of JDK regular expressions from syntax trees.
 * It accepts a syntax tree of a regular expression and generates a regular
 * expression according to syntax rules of JDK's class
 * {@link java.util.regex.Pattern Pattern}.
 *
 * @author  Marian Petras
 */
class Generator {

    /** buffer where the regular expression is being built */
    private StringBuilder buf = new StringBuilder(20);

    /** string to put in place of tokens */
    private Map<String,String> tokenReplacements;

    /** */
    private boolean generatingSetOfChars = false;


    /**
     * Generates a regular expression from a syntax tree.
     *
     * @param  parseTree  root of a syntax tree to create a regular expression
     *                    from 
     * @return  generated regular expression;
     *          or <code>null</code> if the argument was <code>null</code>
     */
    public static String generateRegexp(TreeNode parseTree) {
        return generateRegexp(parseTree, null);
    }


    /**
     * Generates a regular expression from a syntax tree.
     *
     * @param  parseTree  root of a syntax tree to create a regular expression
     *                    from 
     * @param  tokenReplacements  maps token names to strings to be put in place
     *                            of them, or <code>null</code> to ignore tokens
     * @return  generated regular expression;
     *          or <code>null</code> if the argument was <code>null</code>
     */
    public static String generateRegexp(TreeNode parseTree, Map<String,String> tokenReplacements) {
        if (parseTree == null) {
            return null;
        }

        Generator g = new Generator();
        g.setTokenReplacements(tokenReplacements);
        g.generate(parseTree);
        return g.buf.toString();
    }


    /** */
    private static String quoteString(String string) {
        if (string.length() == 0) {
            return string;
        }

        StringBuilder buf;

        int startIndex = 0;
        int endIndex = string.indexOf('\\');                           //NOI18N

        if (endIndex == -1) {
            buf = new StringBuilder(string.length() + 4);
            buf.append("\\Q").append(string).append("\\E");             //NOI18N
        } else {
            buf = new StringBuilder(string.length() + 16);
            do {
                if (endIndex != startIndex) {
                    buf.append("\\Q");                                  //NOI18N
                    buf.append(string.substring(startIndex, endIndex));
                    buf.append("\\E");                                  //NOI18N
                }
                buf.append('\\').append('\\');                                    //NOI18N
                startIndex = endIndex + 1;
                endIndex = string.indexOf('\\', startIndex);           //NOI18N
            } while (endIndex != -1);
            if (startIndex != string.length()) {
                buf.append("\\Q");                                      //NOI18N
                buf.append(string.substring(startIndex));
                buf.append("\\E");                                      //NOI18N
            }
        }
        return buf.toString();
    }


    /** */
    private void setTokenReplacements(Map<String,String> tokenReplacements) {
        if ((tokenReplacements != null) && tokenReplacements.isEmpty()) {
            tokenReplacements = null;
        }
        this.tokenReplacements = tokenReplacements;

        if (tokenReplacements != null) {
            quoteTokenReplacements();
        }
    }


    /** */
    private void quoteTokenReplacements() {
        if (tokenReplacements == null || tokenReplacements.isEmpty()) {
            return;
        }

        for (Map.Entry<String,String> entry : tokenReplacements.entrySet()) {
            entry.setValue(quoteString(entry.getValue()));
        }
    }


    /** */
    private void generate(TreeNode treeNode) {
        List<TreeNode> children = treeNode.getChildren();
        int tokenType = treeNode.getTokenType();
        Object attribs = treeNode.getAttribs();
        char charType;
        switch (tokenType) {
            case TreeNode.CHAR:
                char ch = ((Character) attribs).charValue();
                switch (ch) {
                    case '\t':
                        buf.append('\\').append('t');
                        break;

                    case '\n':
                        buf.append('\\').append('n');
                        break;

                    case '\r':
                        buf.append('\\').append('r');
                        break;

                    case '\f':
                        buf.append('\\').append('f');
                        break;

                    case '\\':
                        buf.append('\\').append('\\');
                        break;

                    default:
                        if (!generatingSetOfChars
                               && ("^$|*+?.()[]{}".indexOf(ch) != -1)) {//NOI18N
                            buf.append('\\');
                        }
                        buf.append(ch);
                        break;
                }
                break;

            case TreeNode.METACHAR:
                charType = ((Character) attribs).charValue();
                if (charType == '.') {
                    buf.append('.');
                } else {
                    buf.append('\\').append(charType);            //    \b or \B
                }
                break;

            case TreeNode.QUANTIFIER:
                if (attribs instanceof Character) {
                    charType = ((Character) attribs).charValue();
                    buf.append(charType);
                } else {
                    String type = (String) attribs;
                    buf.append('{');
                    generate(children.get(0));             //Integer - low limit
                    if (type.length() > 3) {               //"{n,}" or "{n,n}"
                        buf.append(',');
                        if (type.length() == 5) {          //"{n,n}"
                            generate(children.get(1));     //- high limit
                        }
                    }
                    buf.append('}');
                }
                break;

            case TreeNode.Q_REGEXP:
                generate(children.get(0));
                if (children.size() == 2) {
                    generate(children.get(1));
                }
                break;

            case TreeNode.RANGE:
                generate(children.get(0));
                buf.append('-');
                generate(children.get(1));
                break;

            case TreeNode.SET:
                buf.append('[');
                if (attribs != null) {
                    buf.append((String) attribs);
                }
                if (children != null) {
                    generatingSetOfChars = true;
                    if (children.size() == 1) {
                        generate(children.get(0));
                    } else {
                        for (TreeNode child : children) {
                            generate(child);
                        }
                    }
                    generatingSetOfChars = false;
                }
                buf.append(']');
                break;

            case TreeNode.SIMPLE_REGEXP:
                if (children != null) {
                    if (children.size() == 1) {
                        generate(children.get(0));
                    } else {
                        for (TreeNode child : children) {
                            generate(child);
                        }
                    }
                }
                break;

            case TreeNode.SUBEXPR:
                buf.append('(').append('?').append(':');
                generate(children.get(0));
                buf.append(')');
                break;

            case TreeNode.MULTI_REGEXP:
                generate(children.get(0));
                if (children.size() > 1) {
                    Iterator<TreeNode> i = children.iterator();
                    i.next();                               //skip the first one
                    do {
                        buf.append('|');
                        generate(i.next());
                    } while (i.hasNext());
                }
                break;

            case TreeNode.NUMBER:
                buf.append(attribs.toString());
                break;

            case TreeNode.UNICODE_CHAR:
                int code = ((Integer) attribs).intValue();
                buf.append((char) code);
                break;

            case TreeNode.POSIX_SET:
                buf.append('\\').append('p');
                buf.append('{');
                String className = (String) attribs;
                if (className.equals("ascii")) {                        //NOI18N
                    buf.append("ASCII");                                //NOI18N
                } else if (className.equals("xdigit")) {                //NOI18N
                    buf.append("XDigit");                               //NOI18N
                } else {
                    buf.append(Character.toUpperCase(className.charAt(0)));
                    buf.append(className.substring(1));
                }
                buf.append('}');
                break;

            case TreeNode.REGEXP:
                String attrString = (String) attribs;
                if (attrString != null && attrString.charAt(0) == '^') {
                    buf.append('^');
                }
                if (children != null) {
                    generate(children.get(0));
                }
                if (attrString != null && (attrString.length() == 2
                                           || attrString.charAt(0) == '$')) {
                    buf.append('$');
                }
                break;

            case TreeNode.TOKEN:
                String tokenName = (String) attribs;
                String replacement = tokenReplacements != null
                                     ? tokenReplacements.get(tokenName)
                                     : null;
                if (replacement != null) {
                    buf.append('(').append('?').append(':');
                    buf.append(replacement);
                    buf.append(')');
                } else {
                    buf.append('{').append(tokenName).append('}');
                }
                break;

            default:
                assert false;
                break;
        }
    }

}
