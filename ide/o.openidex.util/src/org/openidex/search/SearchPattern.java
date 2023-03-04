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
