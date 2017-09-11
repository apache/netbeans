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
 *
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
