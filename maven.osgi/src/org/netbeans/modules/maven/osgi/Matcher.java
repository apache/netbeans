/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A matcher for a LIST of PATTERN,
 * see http://www.aqute.biz/Bnd/Format and http://felix.apache.org/site/apache-felix-maven-bundle-plugin-bnd.html
 *
 * Main difference from PATTERN description is how we treat a trailing '.*'
 *
 * @author mkleint, folarte
 *
 */
class Matcher {
    
    /**
     * Auxiliary class for each of the PATTERNs in the LIST.
     */
    private static class Item {

        /**
         * Value to return from Matcher.matches method if this item matchs
         * the package name.
         */
        final boolean returnOnMatch;
        /**
         * Exact value given. Only necessary for null patterns,
         * but kept for easier debugging.
         */
        final String exact;
        /**
         * Value translated to a pattern if it contains * or ?.
         * Null if not, in which case a simple equals match against
         * exact is enough.
         */
        final Pattern pattern;

        public Item(boolean returnOnMatch, String exact, Pattern pattern) {
            this.returnOnMatch = returnOnMatch;
            this.exact = exact;
            this.pattern = pattern;

        }

        boolean matches(String packageName) {
            return (pattern != null)
                    ? pattern.matcher(packageName).matches()
                    : exact.equals(packageName);
        }
     }

    /**
     * Precompiled PATTERNs on the LIST.
     */    
    private final Item[] items;
    
    /**
     * Value to be returned when package is not matched by any of items.
     */
    private final boolean unmatchedValue;

    /**
     * Compile a package pattern to an equivalent java.regex.Pattern.
     * @param value package pattern to compile.
     * @return equivalent pattern or null if the pattern is not needed,
     * because the value did not contain any ? or *.
     *
     */
    private static Pattern toPattern(String value) {
        int l = value.length();
        /**
         * .* at end of pattern is special in package patterns, as
         * a.* matches both 'a' and 'a.b', so a simple translation to
         * "a\\..*" will not do it. We strip it here and reconstruct
         * it later.
         */
        boolean endsDotStar = value.endsWith(".*");
        if (endsDotStar) {
            l -= 2; // Do not parse trailing .*
        }
        /**
         * So far the only possible wildcard is the end one.
         */
        boolean hasWildcards = endsDotStar;

        /**
         * Translate filename like pattern to regex pattern.
         * Remember if we found a wildcard,
         * Does not work if someone feeds it strange things, but bnd plugin
         * has the same problem, so do not bother.
         */
        StringBuilder sb = new StringBuilder(2 * l);
        for (int i = 0; i < l; ++i) {
            final char c = value.charAt(i);
            switch (c) {
                case '.':
                    sb.append("\\.");
                    break;
                case '*':
                    sb.append(".*");
                    hasWildcards = true;
                    break;
                case '?':
                    sb.append(".?");
                    hasWildcards = true;
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        if (hasWildcards) {
            // Adjust stripped .* at the end.
            if (endsDotStar) {
                // Optional non capturing group of ( dot, any )
                sb.append("(?:\\..*)?");
            }
            try {
                return Pattern.compile(sb.toString());
            } catch(PatternSyntaxException px) {
                Logger.getLogger(Matcher.class.getName()).log(Level.WARNING, null, px);
                return null; // what else ?
            }
        } else {
            // No wildcard found, avoid compiling pattern so the Item will
            // use a simple equals test.
            return null; // No pattern needed.
        }
    }    

    Matcher(String pattern) {
        List<Item> list = new ArrayList<Item>();
        boolean unmatched = false; // Default for no items.
        if (pattern != null && !pattern.contains("${")) {
            String[] strItems = pattern.split(",");
            for (String strItem : strItems) {

                int semic = strItem.indexOf(';');
                String value = ((semic < 0)
                        ? strItem
                        : strItem.substring(0, semic)).trim();

                if (!value.isEmpty()) { // In case of ",,"
                    boolean returnOnMatch = true;
                    if (value.startsWith("!")) {
                        returnOnMatch = false;
                        value = value.substring(1);
                    }
                    /**
                     * If * or !* is found we do have the value to return when none
                     * of the (previous) items matches, and we have reached the end,
                     * as [!]* forces a return.
                     */
                    if ("*".equals(value)) {
                        unmatched = returnOnMatch; // In case it was !*
                        break; // Stop parsing the list.
                    }
                    list.add(new Item(returnOnMatch, value, toPattern(value)));
                 }
            }
        }
        items = list.toArray(new Item[list.size()]);
        unmatchedValue = unmatched;
    }
    
    
    boolean matches(String packageName) {
        for (Item item : items) {
            if (item.matches(packageName)) {
                return item.returnOnMatch;
             }
         }
        return unmatchedValue;
    }
}
