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

package org.netbeans.editor.ext;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.io.IOException;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;

/**
* Generator of code used for matching the keywords or more generally some
* group of words.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class KeywordMatchGenerator {

    private static final String USAGE
    = "Usage: java org.netbeans.editor.ext.KeywordMatchGenerator [options]" // NOI18N
      + " keyword-file [match-function-name]\n\n" // NOI18N
      + "Options:\n" // NOI18N
      + "  -i Ignore case in matching\n" // NOI18N
      + "  -s Input is in 'input' String or StringBuffer instead of char buffer\n" // NOI18N
      + "\nGenerator of method that matches" // NOI18N
      + " the keywords provided in the file.\n" // NOI18N
      + "Keywords in the file must be separated by spaces or new-lines" // NOI18N
      + " and they don't need to be sorted.\n"; // NOI18N

    private static final String UNKNOWN_OPTION = " is unknown option.\n"; // NOI18N

    public static final String IGNORE_CASE = "-i"; // NOI18N

    public static final String USE_STRING = "-s"; // NOI18N

    private static final String DEFAULT_METHOD_NAME = "match"; // NOI18N

    private static final String[] OPTION_LIST = { IGNORE_CASE, USE_STRING };

    /** The list of keywords */
    private String kwds[];

    /** Maximum length of keyword */
    private int maxKwdLen;

    /** Options */
    private HashMap<String, String> options = new HashMap<>();

    private HashMap<String, String> kwdConstants = new HashMap<>();

    /** Provide indentation (default 2 spaces) */
    private String indent(int cnt) {
        StringBuffer sb = new StringBuffer();

        while(cnt-- > 0) {
            sb.append("  "); // NOI18N
        }
        return sb.toString();
    }

    protected void initScan(String methodName) {

        if (methodName == null) {
            methodName = DEFAULT_METHOD_NAME;
        }

        // write keyword constants table
        appendString("\n"); // NOI18N
        for (int i = 0; i < kwds.length; i++) {
            appendString(indent(1) + "public static final int " + kwdConstants.get(kwds[i]) // NOI18N
                         + " = " + i + ";\n"); // NOI18N
        }
        appendString("\n"); // NOI18N

        // write method header
        appendString(indent(1) + "public static int "); // NOI18N
        appendString(methodName);
        if (options.get(USE_STRING) != null) {
            appendString("(String buffer, int offset, int len) {\n"); // NOI18N
        } else {
            appendString("(char[] buffer, int offset, int len) {\n"); // NOI18N
        }
        appendString(indent(2) + "if (len > " + maxKwdLen + ")\n"); // NOI18N
        appendString(indent(3) + "return -1;\n"); // NOI18N
    }

    public void scan() {
        scan(0, kwds.length, 0, 2, 0);
    }

    protected void finishScan() {
        appendString(indent(1) + "}\n\n"); // NOI18N
    }

    public void addOption(String option) {
        options.put(option, option);
    }

    protected String getKwdConstantPrefix() {
        return ""; // "KWD_"; // NOI18N
    }

    protected String getKwdConstant(String kwd) {
        return kwdConstants.get(kwd);
    }

    protected boolean upperCaseKeyConstants() {
        return true;
    }

    /** Parse the keywords from a string */
    private void parseKeywords(String s) {
        ArrayList<String> keyList = new ArrayList<>();
        StringTokenizer strTok = new StringTokenizer(s);

        try {
            while(true) {
                String key = strTok.nextToken();
                int keyLen = key.length();
                maxKwdLen = Math.max(maxKwdLen, keyLen);
                keyList.add(key);
                kwdConstants.put(key, getKwdConstantPrefix()
                                 + (upperCaseKeyConstants() ? key.toUpperCase() : key));
            }
        } catch(NoSuchElementException e) {
            // no more elements
        }

        kwds = new String[keyList.size()];
        keyList.toArray(kwds);
        Arrays.sort(kwds);
    }

    protected String getCurrentChar() {
        boolean useString = (options.get(USE_STRING) != null);
        boolean ignoreCase = (options.get(IGNORE_CASE) != null);

        if(useString) {
            return ignoreCase ? "Character.toLowerCase(buffer.charAt(offset++))" // NOI18N
                   : "buffer.charAt(offset++)"; // NOI18N
        } else {
            return ignoreCase ? "Character.toLowerCase(buffer[offset++])" // NOI18N
                   : "buffer[offset++]"; // NOI18N
        }
    }

    private void appendCheckedReturn(String kwd, int offset, int indent) {
        appendString(indent(indent) + "return (len == " // NOI18N
                     + kwd.length());

        int kwdLenM1 = kwd.length() - 1;
        for(int k = offset; k <= kwdLenM1; k++) {
            appendString("\n" + indent(indent + 1) + "&& "); // NOI18N
            appendString(getCurrentChar() + " == '" + kwd.charAt(k) + "'"); // NOI18N
        }

        appendString(")\n" + indent(indent + 2) + "? " + getKwdConstant(kwd) + " : -1;\n"); // NOI18N
    }

    protected void appendString(String s) {
        System.out.print(s);
    }

    /** Scan the keywords and generate the output. This method is initially
    * called with the full range of keywords and offset equal to zero.
    * It recursively calls itself to scan the subgroups.
    * @param indFrom index in kwds[] where the subgroup of keywords starts
    * @param indTo index in kwds[] where the subgroup of keywords ends
    * @param offset current horizontal offset. It's incremented as the subgroups
    *   are recognized. All the characters prior to offset index are the same
    *   in all keywords in the group.
    */
    private void scan(int indFrom, int indTo, int offset, int indent, int minKwdLen) {
        //    System.out.println(">>>DEBUG<<< indFrom=" + indFrom + ", indTo=" + indTo + ", offset=" + offset + ", indent=" + indent + ", minKwdLen="+ minKwdLen); // NOI18N
        int maxLen = 0;
        for (int i = indFrom; i < indTo; i++) {
            maxLen = Math.max(maxLen, kwds[i].length());
        }

        int same;
        int minLen;
        do {
            minLen = Integer.MAX_VALUE;
            // Compute minimum and maximum keyword length in the current group
            for (int i = indFrom; i < indTo; i++) {
                minLen = Math.min(minLen, kwds[i].length());
            }

            //      System.out.println(">>>DEBUG<<< while(): minLen=" + minLen + ", minKwdLen=" + minKwdLen); // NOI18N
            if (minLen > minKwdLen) {
                appendString(indent(indent) + "if (len <= " + (minLen - 1) + ")\n"); // NOI18N
                appendString(indent(indent + 1) + "return -1;\n"); // NOI18N
            }

            // Compute how many chars from current offset on are the same
            // in all keywords in the current group
            same = 0;
            boolean stop = false;
            for (int i = offset; i < minLen; i++) {
                char c = kwds[indFrom].charAt(i);
                for (int j = indFrom + 1; j < indTo; j++) {
                    if (kwds[j].charAt(i) != c) {
                        stop = true;
                        break;
                    }
                }
                if (stop) {
                    break;
                }
                same++;
            }

            //      System.out.println(">>>DEBUG<<< minLen=" + minLen + ", maxLen=" + maxLen + ", same=" + same); // NOI18N

            // Add check for all the same chars
            if (same > 0) {
                appendString(indent(indent) + "if ("); // NOI18N
                for (int i = 0; i < same; i++) {
                    if (i > 0) {
                        appendString(indent(indent + 1) + "|| "); // NOI18N
                    }
                    appendString(getCurrentChar() + " != '" + kwds[indFrom].charAt(offset + i) + "'"); // NOI18N
                    if (i < same - 1) {
                        appendString("\n"); // NOI18N
                    }
                }
                appendString(")\n" + indent(indent + 2) + "return -1;\n"); // NOI18N

            }

            // Increase the offset to the first 'non-same' char
            offset += same;

            // If there's a keyword with the length equal to the current offset
            // it will be first in the (sorted) group and it will be matched now
            if (offset == kwds[indFrom].length()) {
                appendString(indent(indent) + "if (len == " + offset + ")\n"); // NOI18N
                appendString(indent(indent + 1) + "return " // NOI18N
                             + getKwdConstant(kwds[indFrom]) + ";\n"); // NOI18N
                indFrom++; // increase starting index as first keyword already matched
                if (offset >= minLen) {
                    minLen = offset + 1;
                }
            }

            minKwdLen = minLen; // minLen already tested, so assign new minimum

        } while (same > 0 && indFrom < indTo);

        // If there are other chars at the end of any keyword,
        // add the switch statement
        if (offset < maxLen) {
            appendString(indent(indent) + "switch (" + getCurrentChar() + ") {\n"); // NOI18N

            // Compute subgroups
            int i = indFrom;
            while(i < indTo) {
                // Add the case statement
                char actChar = kwds[i].charAt(offset);
                appendString(indent(indent + 1) + "case '" + actChar + "':\n"); // NOI18N

                // Check whether the subgroup will have more than one keyword
                int subGroupEndInd = i + 1;
                while(subGroupEndInd < indTo
                        && kwds[subGroupEndInd].length() > offset
                        && kwds[subGroupEndInd].charAt(offset) == actChar
                     ) {
                    subGroupEndInd++;
                }

                if(subGroupEndInd > i + 1) { // more than one keyword in subgroup
                    scan(i, subGroupEndInd, offset + 1, indent + 2, minLen);
                } else { // just one keyword in the subgroup
                    appendCheckedReturn(kwds[i], offset + 1, indent + 2);
                }

                // advance current index to the end of current subgroup
                i = subGroupEndInd;
            }

            appendString(indent(indent + 1) + "default:\n"); // NOI18N
            appendString(indent(indent + 2) + "return -1;\n"); // NOI18N
            appendString(indent(indent) + "}\n"); // NOI18N
        } else { // no add-on chars, keyword not found in this case
            appendString(indent(indent) + "return -1;\n"); // NOI18N
        }

    }

    /** Main method */
    public static void main(String args[]) {
        KeywordMatchGenerator km = new KeywordMatchGenerator();

        // parse options
        int argShift;
        for (argShift = 0; argShift < args.length; argShift++) {
            int j;
            if (args[argShift].charAt(0) != '-') {
                break; // no more options
            }
            for (j = 0; j < OPTION_LIST.length; j++) {
                if (args[argShift].equals(OPTION_LIST[j])) {
                    km.addOption(OPTION_LIST[j]);
                    break;
                }
            }
            if (j == OPTION_LIST.length) {
                System.err.println("'" + args[argShift] + "'" + UNKNOWN_OPTION); // NOI18N
                System.err.println(USAGE);
                return;
            }
        }

        // check count of mandatory args
        if (args.length - argShift < 1) {
            System.err.println(USAGE);
            return;
        }

        // read keyword file
        String kwds = null;
        try {
            File f = new File(args[argShift]);
            if (!f.exists()) {
                System.err.println("Non-existent file '" + args[argShift] + "'"); // NOI18N
                return;
            }
            char arr[] = new char[(int)f.length()];
            Reader isr = new FileReader(f);

            int n = 0;
            while (n < f.length()) {
                int count = isr.read(arr, n, (int)f.length() - n);
                if (count < 0)
                    break;
                n += count;
            }

            kwds = new String(arr);
        } catch(IOException e) {
            // IO exception
            System.err.println("Cannot read from keyword file '" + args[argShift] + "'"); // NOI18N
            return;
        }

        // Check for optional method name
        String methodName = null;
        if (args.length - argShift >= 2) {
            methodName = args[argShift + 1];
        }

        // generate
        km.parseKeywords(kwds);
        km.initScan(methodName);
        km.scan();
        km.finishScan();

    }


}
