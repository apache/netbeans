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
 * Software is Sun Microsystems, Inc. Portions Copyright 2005 Sun
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
package org.netbeans.api.search;

import org.openide.util.NbBundle;

/**
 * Pattern describes the search conditions
 *
 * @author Martin Roskanin
 */
public final class SearchPattern {

    /**
     * Specifies how the pattern is matched to the searched text.
     *
     * Please note that more items can be added to the enum in the future.
     *
     * @since api.search/1.11
     */
    @NbBundle.Messages({
        "LBL_MatchType_Literal=Literal",
        "LBL_MatchType_Basic_Wildcards=Basic Wildcards",
        "LBL_MatchType_Regular_Expression=Regular Expression"
    })
    public static enum MatchType {

        /**
         * Match the pattern literally.
         */
        LITERAL(Bundle.LBL_MatchType_Literal(), 'L'),
        /**
         * The pattern can contain basic wildcards, star (*) for any string and
         * questionaire (?) for any character. The escape character for these
         * wildcards is backslash (\).
         */
        BASIC(Bundle.LBL_MatchType_Basic_Wildcards(), 'r'),
        /**
         * The pattern follows java.util.regex.Pattern syntax.
         */
        REGEXP(Bundle.LBL_MatchType_Regular_Expression(), 'R');
        private final String displayName;
        private final char canonicalPatternFlag;

        private MatchType(String displayName, char canonicalPatternFlag) {
            this.displayName = displayName;
            this.canonicalPatternFlag = canonicalPatternFlag;
        }

        @Override
        public String toString() {
            return displayName;
        }

        private char getCanonicalPatternFlag() {
            return canonicalPatternFlag;
        }

        private static MatchType fromCanonicalPatternFlag(char ch) {
            switch (ch) {
                case 'R':
                    return REGEXP;
                case 'r':
                    return BASIC;
                case 'L':
                    return LITERAL;
                default:
                    return BASIC;
            }
        }

        private static boolean isCanonicalPatternFlag(char ch) {
            for (MatchType mt : MatchType.values()) {
                if (mt.getCanonicalPatternFlag() == ch) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * SearchExpression - a text to search
     */
    private String searchExpression;
    /**
     * if true, only whole words were searched
     */
    private boolean wholeWords;
    /**
     * if true, case sensitive search was preformed
     */
    private boolean matchCase;
    /**
     * match type of this pattern
     */
    private MatchType matchType;

    /**
     * Creates a new instance of SearchPattern
     *
     * @param searchExpression a searched text
     * @param wholeWords if true, only whole words were searched
     * @param matchCase if true, case sensitive search was preformed
     * @param regExp if true, regular expression search was performed
     */
    private SearchPattern(String searchExpression, boolean wholeWords,
            boolean matchCase, MatchType matchType) {
        this.searchExpression = searchExpression;
        this.wholeWords = wholeWords;
        this.matchCase = matchCase;
        this.matchType = matchType;
    }

    /**
     * Creates a new SearchPattern in accordance with given parameters
     *
     * @param searchExpression non-null String of a searched text
     * @param wholeWords if true, only whole words were searched
     * @param matchCase if true, case sensitive search was preformed
     * @param regExp if true, regular expression search was performed; if false,
     * search with basic wildcards was performed
     * @return a new SearchPattern in accordance with given parameters
     */
    public static SearchPattern create(String searchExpression, boolean wholeWords,
            boolean matchCase, boolean regExp) {
        return new SearchPattern(searchExpression, wholeWords, matchCase,
                regExp ? MatchType.REGEXP : MatchType.LITERAL);
    }

    /**
     * Creates a new SearchPattern in accordance with given parameters
     *
     * @param searchExpression non-null String of a searched text
     * @param wholeWords if true, only whole words were searched
     * @param matchCase if true, case sensitive search was preformed
     * @param matchType match type
     * @return a new SearchPattern in accordance with given parameters
     *
     * @since api.search/1.11
     */
    public static SearchPattern create(String searchExpression,
            boolean wholeWords, boolean matchCase, MatchType matchType) {
        return new SearchPattern(searchExpression, wholeWords, matchCase,
                matchType);
    }

    /**
     * @return searchExpression
     */
    public String getSearchExpression() {
        return searchExpression;
    }

    /**
     * @return true if the wholeWords parameter was used during search
     * performing
     */
    public boolean isWholeWords() {
        return wholeWords;
    }

    /**
     * @return true if the matchCase parameter was used during search performing
     */
    public boolean isMatchCase() {
        return matchCase;
    }

    /**
     * @return true if the regExp parameter was used during search performing
     */
    public boolean isRegExp() {
        return matchType == MatchType.REGEXP;
    }

    /**
     * Get type of this pattern.
     *
     * @since api.search/1.11
     */
    public MatchType getMatchType() {
        return matchType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SearchPattern)) {
            return false;
        }
        SearchPattern sp = (SearchPattern) obj;
        return (this.searchExpression.equals(sp.getSearchExpression())
                && this.wholeWords == sp.isWholeWords()
                && this.matchCase == sp.isMatchCase()
                && this.matchType == sp.matchType);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + (this.wholeWords ? 1 : 0);
        result = 37 * result + (this.matchCase ? 1 : 0);
        result = 37 * result + (this.matchType.hashCode());
        result = 37 * result + this.searchExpression.hashCode();
        return result;
    }

    /**
     * Create new instance with "search expression" set to passed value, and
     * other values copied from this instance.
     *
     */
    public SearchPattern changeSearchExpression(String expression) {
        if ((expression == null && this.searchExpression == null)
                || (expression != null
                && expression.equals(this.searchExpression))) {
            return this;
        } else {
            return SearchPattern.create(expression, wholeWords,
                    matchCase, matchType);
        }
    }

    /**
     * Create new instance with "whole words" set to passed value, and other
     * values copied from this instance.
     *
     */
    public SearchPattern changeWholeWords(boolean wholeWords) {
        if (this.wholeWords == wholeWords) {
            return this;
        } else {
            return SearchPattern.create(searchExpression, wholeWords,
                    matchCase, matchType);
        }
    }

    /**
     * Create new instance with "match case" set to passed value, and other
     * values copied from this instance.
     *
     */
    public SearchPattern changeMatchCase(boolean matchCase) {
        if (this.matchCase == matchCase) {
            return this;
        } else {
            return SearchPattern.create(searchExpression, wholeWords,
                    matchCase, matchType);
        }
    }

    /**
     * Create new instance with "regular expression" set to passed value, and
     * other values copied from this instance.
     *
     */
    public SearchPattern changeRegExp(boolean regExp) {
        if (this.isRegExp() == regExp) {
            return this;
        } else {
            return SearchPattern.create(searchExpression, wholeWords,
                    matchCase, regExp);
        }
    }

    /**
     * Create new instance with "match type" set to passed value, and other
     * values copied from this instance.
     *
     * @since api.search/1.11
     */
    public SearchPattern changeMatchType(MatchType matchType) {
        if (this.matchType == matchType) {
            return this;
        } else {
            return SearchPattern.create(searchExpression, wholeWords, matchCase,
                    matchType);
        }
    }

    String toCanonicalString() {
        char m = isMatchCase() ? 'M' : 'm';
        char r = matchType.getCanonicalPatternFlag();
        char w = isWholeWords() ? 'W' : 'w';
        return "" + m + r + w + "-" + getSearchExpression(); //NOI18N
    }

    static SearchPattern parsePattern(String canonicalString) {
        //format mrw-findwhat
        if (canonicalString == null
                || Character.toUpperCase(canonicalString.charAt(0)) != 'M'
                || !MatchType.isCanonicalPatternFlag(canonicalString.charAt(1))
                || Character.toUpperCase(canonicalString.charAt(2)) != 'W'
                || canonicalString.charAt(3) != '-') {
            return null;
        }
        boolean matchCase = Character.isUpperCase(canonicalString.charAt(0));
        MatchType matchType = MatchType.fromCanonicalPatternFlag(
                canonicalString.charAt(1));
        boolean wholeWords = Character.isUpperCase(canonicalString.charAt(2));
        String findWhat = canonicalString.substring(4);
        return new SearchPattern(findWhat, wholeWords, matchCase, matchType);
    }

}
