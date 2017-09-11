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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.jellytools.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;

/** Filters string, you can simple use replaceString() method or create string
 * filter for more sophisticated filtering.
 *
 * <p>
 * Usage:<br>
 * <pre>
 * StringFilter sf = new StringFilter();
 * // remove 1st comment
 * sf.addReplaceFilter("/*",  "*&#47;", "");
 * // replace all multiple spaces
 * sf.addReplaceAllFilter("  ", " ");
 * // change author name
 * sf.addReplaceFilter("author: ", "", "author: spl@sun.com");
 * String string = "/* comment *&#47;    4s  2s   3s author: xxx@sun.com";
 * String result = sf.filter(string);
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 * @see #replaceString(String original, String begin, String end, String replace)
 */
public class StringFilter {
    ArrayList<Pattern> filter = new ArrayList<Pattern>();
    
    /** Adds replace pattern into Filter.
     * @see #replaceString(String original, String begin, String end, String replace )
     * @param begin the begin of substring to be find
     * @param end the end of substring to be find
     * @param replace text to replace
     */
    public void addReplaceFilter(String begin, String end, String replace) {
        filter.add(new Pattern(begin, end, replace));
    }
    
    /** Adds replace pattern into Filter.
     * @see #replaceString(String original, String begin, String end, String replace )
     * @param find text to find
     * @param replace text to replace
     */
    public void addReplaceAllFilter(String find, String replace) {
        filter.add(new Pattern(find, find, replace));
    }
    
    /** Filters string.
     * @param str text to filter
     * @return filtred string
     */
    public String filter(String str) {
        for (int i = 0; i < filter.size(); i++) {
            Pattern p = filter.get(i);
            if (p != null) {
                str = replaceString(str, p.begin, p.end, p.replace);
            }
        }
        return str;
    }
    
    /** Finds substring which starts with first occurrence of 'begin' and ends
     * with nearest next occurrence of 'end' and replaces it with 'replace '.<p>
     *
     * Usage:
     * <br><pre>
     * replaceString("a-bcd-ef",    "b",  "d",  "")   => a--ef
     * replaceString("abc-def",     "",   "c",  "")   => -def
     * replaceString("ab-cdef",     "c",  "",   "")   => ab-
     * replaceString("ab-cdef-ab",  "ab", "ab", "AB") => AB-cdef-AB
     * replaceString("abcdef",      "",    "",  "AB") => abcdef
     * </pre>
     *
     * @return filtred string
     * @param replace text to replace
     * @param original the original string
     * @param begin the begin of substring to be find
     * @param end the end of substring to be find
     */
    public static String replaceString(String original, String begin, String end, String replace ) {
        boolean replaceAll = false;
        int from;
        int to;
        int offset = 0;
        
        if (isEmpty(original) || (isEmpty(begin) && isEmpty(end))) return original;
        if (begin.equals(end)) replaceAll = true;
        do {
            from = isEmpty(begin) ? 0 : original.indexOf(begin, offset);
            if (from < 0)
                break;
            if (isEmpty(end)) {
                to = original.length();
            } else {
                to = original.indexOf(end, from);
                if (to < 0)
                    break;
                to += end.length();
            }
            original = original.substring(0, from) + replace  + original.substring(to, original.length());
            offset = from + replace.length();
        } while (replaceAll);
        
        return original;
    }
    
    /** Finds and replace all substrings.
     * @see #replaceString(String original, String begin, String end, String replace)
     * @param original the original string
     * @param find text to find
     * @param replace text to replace
     * @return filtred string
     */
    public static String replaceStringAll(String original, String find, String replace) {
        return replaceString(original, find, find, replace);
    }
    
    private static boolean isEmpty(String s) {
        return s == null || s.equals("");
    }
    
    /** Filters input string to a PrintStream.
     * @param input string to be filtered
     * @param output stream to write results in
     */    
    public void filterLinesToStream (String input, PrintStream output) {
        BufferedReader br = new BufferedReader (new StringReader (input));
        try {
            for (;;) {
                String str = br.readLine();
                if (str == null)
                    break;
                str = filter (str);
                output.println (str);
            }
        } catch (IOException e) {
        } finally {
            try { br.close (); } catch (IOException e) {}
        }
    }
    
    private static class Pattern {
        private String begin;
        private String end;
        private String replace;
        
        public Pattern(String begin, String end, String replace) {
            this.begin = begin;
            this.end = end;
            this.replace = replace;
        }
    }
}
