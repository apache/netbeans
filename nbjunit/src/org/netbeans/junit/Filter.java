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

package org.netbeans.junit;

import java.util.LinkedList;
import java.util.List;

/**
 * A helper class, which holds informatino about filtered tests
 */
public class Filter {
    private IncludeExclude inc[] = new IncludeExclude[] {};
    private IncludeExclude exc[] = new IncludeExclude[] {};

    /** Creates new Filter */
    public Filter() {
    }

    public void setIncludes(IncludeExclude includes[]) {
        inc = arrayCopy(includes);
    }

    public void setExcludes(IncludeExclude excludes[]) {
        exc = arrayCopy(excludes);
    }

    public IncludeExclude [] getIncludes() {
        return arrayCopy(inc);
    }
    
    public IncludeExclude [] getExcludes() {
        return arrayCopy(exc);
    }
    
    public boolean isIncluded(String name) {
        int i;
        for(i = 0; i < inc.length; i++)
            if (inc[i].getName() == null || match(inc[i].getName(), name))
                break;

        if (0 < inc.length && i == inc.length) {
            return false;
        }
        
        for(i = 0; i < exc.length; i++)
            if (match(exc[i].getName(), name))
                break;
        
        return i == exc.length;
    }
    
    public String getExpectedFail(String name) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < inc.length; i++) {
            if (inc[i].getName() == null || match(inc[i].getName(), name)) {
                if (inc[i].getExpectedFail() != null) {
                    if (sb.length() != 0) 
                        sb.append("; ");
                    sb.append(inc[i].getExpectedFail());
                }
            }
        }
        if (sb.length() == 0)
            return null;
        else 
            return sb.toString();
    }
    
    /**
     * Matches a string against a pattern. The pattern contains two special
     * characters:
     * '*' which means zero or more characters,
     * '?' which means one and only one character.
     *
     * This code was stolen from Ant's DirectoryScanner.match(String, String) function.
     *
     * @param pattern the (non-null) pattern to match against
     * @param str     the (non-null) string that must be matched against the
     *                pattern
     *
     * @return <code>true</code> when the string matches against the pattern,
     *         <code>false</code> otherwise.
     */
    public static boolean match(String pattern, String str) {

        if (null == pattern && null == str)
            return true;
        
        if (null == pattern || null == str)
            return false;

        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd   = patArr.length-1;
        int strIdxStart = 0;
        int strIdxEnd   = strArr.length-1;
        char ch;

        boolean containsStar = false;
        for (int i = 0; i < patArr.length; i++) {
            if (patArr[i] == '*') {
                containsStar = true;
                break;
            }
        }

        if (!containsStar) {
            // No '*'s, so we make a shortcut
            if (patIdxEnd != strIdxEnd) {
                return false; // Pattern and string do not have the same size
            }
            for (int i = 0; i <= patIdxEnd; i++) {
                ch = patArr[i];
                if (ch != '?' && ch != strArr[i]) {
                    return false; // Character mismatch
                }
            }
            return true; // String matches against pattern
        }

        if (patIdxEnd == 0) {
            return true; // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while((ch = patArr[patIdxStart]) != '*' && strIdxStart <= strIdxEnd) {
            if (ch != '?' && ch != strArr[strIdxStart]) {
                return false;
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // Process characters after last star
        while((ch = patArr[patIdxEnd]) != '*' && strIdxStart <= strIdxEnd) {
            if (ch != '?' && ch != strArr[strIdxEnd]) {
                return false;
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for (int i = patIdxStart+1; i <= patIdxEnd; i++) {
                if (patArr[i] == '*') {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart+1) {
                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp-patIdxStart-1);
            int strLength = (strIdxEnd-strIdxStart+1);
            int foundIdx  = -1;
strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    ch = patArr[patIdxStart+j+1];
                    if (ch != '?' && ch != strArr[strIdxStart+i+j]) {
                        continue strLoop;
                    }
                }

                foundIdx = strIdxStart+i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx+patLength;
        }

        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (patArr[i] != '*') {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        StringBuffer b = new StringBuffer();
        
        if (0 < inc.length) {
            b.append("-i ");
            for(int i = 0; i < inc.length; i++) {
                b.append(inc[i]);
                b.append(' ');
            }
        }
        
        if (0 < exc.length) {
            b.append("-e ");
            for(int i = 0; i < exc.length; i++) {
                b.append(exc[i]);
                b.append(' ');
            }
        }
        
        return b.toString();
    }
    
    private IncludeExclude [] arrayCopy(IncludeExclude [] orig) {
        List<IncludeExclude> lst = new LinkedList<IncludeExclude>();
        for(int i = 0; i < orig.length; i++)
            if (null != orig[i] && !(orig[i].getName() == null && orig[i].getExpectedFail() == null)) {
                lst.add(orig[i]);
            }
        
        return lst.toArray(new IncludeExclude[0]);
    }
    
    
    public static class IncludeExclude {
        private String name;
        private String expectedFail;
        
        public IncludeExclude() {
        }
        
        public IncludeExclude(String name, String expectedFail) {
            this.name = name;
            this.expectedFail = expectedFail;
        }
        
        public String getName() {
            return name;
        }
        
        public String getExpectedFail() {
            return expectedFail;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void setExpectedFail(String expectedFail) {
            this.expectedFail = expectedFail;
        }
        
        public String toString() {
            return name+":"+expectedFail;
        }
    }
}
