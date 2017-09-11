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

package org.openidex.search;

/**
 * Pattern describes the search conditions
 *
 * @since  org.openidex.util/3 3.5, NB 4.1
 * @author  Martin Roskanin
 */
public final class SearchPattern {

    /** SearchExpression - a text to search */
    private String searchExpression;

    /** if true, only whole words were searched */
    private boolean wholeWords;

    /** if true, case sensitive search was preformed */
    private boolean matchCase;

    /** if true, regular expression search was performed */
    private boolean regExp;

    /** Creates a new instance of SearchPattern 
     *  @param searchExpression a searched text
     *  @param wholeWords if true, only whole words were searched
     *  @param matchCase if true, case sensitive search was preformed
     *  @param regExp if true, regular expression search was performed
     */
    private SearchPattern(String searchExpression, boolean wholeWords,
            boolean matchCase, boolean regExp) {
        this.searchExpression = searchExpression;
        this.wholeWords = wholeWords;
        this.matchCase = matchCase;
        this.regExp = regExp;
    }
 
    /** Creates a new SearchPattern in accordance with given parameters 
     *  @param searchExpression non-null String of a searched text
     *  @param wholeWords if true, only whole words were searched
     *  @param matchCase if true, case sensitive search was preformed
     *  @param regExp if true, regular expression search was performed
     *  @return a new SearchPattern in accordance with given parameters
     */
    public static SearchPattern create(String searchExpression, boolean wholeWords,
            boolean matchCase, boolean regExp){
        return new SearchPattern(searchExpression, wholeWords, matchCase, regExp);
    }
    
    /** @return searchExpression */
    public String getSearchExpression(){
        return searchExpression;
    }
    
    /** @return true if the wholeWords parameter was used during search performing */
    public boolean isWholeWords(){
        return wholeWords;
    }
    
    /** @return true if the matchCase parameter was used during search performing */
    public boolean isMatchCase(){
        return matchCase;
    }
    
    /** @return true if the regExp parameter was used during search performing */
    public boolean isRegExp(){
        return regExp;
    }

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof SearchPattern)){
            return false;
        }
        SearchPattern sp = (SearchPattern)obj;
        return (this.searchExpression.equals(sp.getSearchExpression()) &&
                this.wholeWords == sp.isWholeWords() &&
                this.matchCase == sp.isMatchCase() &&
                this.regExp == sp.isRegExp());
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (this.wholeWords ? 1:0);
        result = 37*result + (this.matchCase ? 1:0);
        result = 37*result + (this.regExp ? 1:0);
        result = 37*result + this.searchExpression.hashCode();
        return result;
    }
    
    String toCanonicalString() {
        char m = isMatchCase() ? 'M' : 'm';
        char r = isRegExp() ? 'R' : 'r';
        char w = isWholeWords() ? 'W' : 'w';
        return "" + m + r + w + "-" + getSearchExpression();
    }

    static SearchPattern parseSearchPattern(String canonicalString) {
        //format mrw-findwhat
        if (canonicalString == null
                || Character.toUpperCase(canonicalString.charAt(0)) != 'M'
                || Character.toUpperCase(canonicalString.charAt(1)) != 'R'
                || Character.toUpperCase(canonicalString.charAt(2)) != 'W'
                || canonicalString.charAt(3) != '-') {
            return null;
        }
        boolean matchCase = Character.isUpperCase(canonicalString.charAt(0));
        boolean regExp = Character.isUpperCase(canonicalString.charAt(1));
        boolean wholeWords = Character.isUpperCase(canonicalString.charAt(2));
        String findWhat = canonicalString.substring(4);
        return new SearchPattern(findWhat, wholeWords, matchCase, regExp);
    }

}
