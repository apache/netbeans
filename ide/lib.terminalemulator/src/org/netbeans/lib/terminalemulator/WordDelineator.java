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

/*
 * "WordDelineator.java"
 * WordDelineator.java 1.6 01/07/26
 */
package org.netbeans.lib.terminalemulator;

/*
 * Class used by Term to find the boundaries of a <i>word</i>, the region
 * of text that gets selected when you double-click.
 *<p>
 * Term has a default WordDelineator which can be changed by using this class
 * as an adapter and overriding either charClass() or findLeft() and
 * findRight() and assigning an object of the resulting class via
 * Term.setWordDelineator().
 */
public class WordDelineator {

    private String delimiters = new String();
    
    private WordDelineator(String delimeters) {
        this.delimiters = delimeters;
    }

    /**
     * Return the <i>character equivalence class</i> of 'c'.
     * <p>
     * This is used by findLeft() and findRight() which operate such that a
     * <i>word</i> is bounded by a change in character class.
     * <p>
     * A character equivalence class is characterised by a number, any number,
     * that is different from numbers for other character classes. For example,
     * this implementation, which is used as the default WordDelineator for Term
     * returns 1 for spaces, 2 for delimiters defined by user in
     * {@link org.netbeans.lib.terminalemulator.support.TermOptions} and 0 for
     * everything else.
     */
    protected int charClass(char c) {
        if (delimiters.indexOf(c) >= 0) {
            return 2;
        } else {
            return 0;
        }
    }

    /**
     * Return index of a char at the beginning of the word.
     * @param buf text we search at (typically a line)
     * @param start where to start search from
     * @param useLineBound should the line bound be treated (if reached) as a word start
     * @return idx of the left bound or -1 if a bound reached with useLineBound flag
     */
    protected int findLeft(StringBuffer buf, int start, boolean useLineBound) {
        int cclass = charClass(buf.charAt(start));

        // go left until a character of differing class is found
        int lx = start;
        boolean success = false;
        while (lx > 0) {
            success = charClass(buf.charAt(lx - 1)) != cclass;
            if (success) {
                break;
            }
            lx--;
        }
        return (!success && useLineBound) ? -1 : lx;
    }

    /**
     * Return index of char past the word.
     * @param buf text we search at (typically a line)
     * @param start where to start search from
     * @param useLineBound should the line bound be treated (if reached) as a word start
     * @return idx of the right bound or -1 if a bound reached with useLineBound flag
     */
    protected int findRight(StringBuffer buf, int start, boolean useLineBound) {
        int cclass = charClass(buf.charAt(start));

        // go right until a character of a differing class is found.
        int rx = start;
        boolean success = false;
        while (rx < buf.length()) {
            success = charClass(buf.charAt(rx)) != cclass;
            if (success) {
                break;
            }
            rx++;
        }
        rx--;
        return (!success && useLineBound) ? -1 : rx;
    }
    
    public static WordDelineator createCustomDelineator(String delimeters) {
        if (delimeters.contains(" ")) { //NOI18N
            return new WordDelineator(delimeters);
        } else {
            return new WordDelineator(delimeters.concat(" ")); //NOI18N
        }
    }
    
    public static WordDelineator createNewlineDelineator() {
        return new WordDelineator("\n"); //NOI18N
    }
}
