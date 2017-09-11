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
 */

package org.netbeans.api.lexer;

import org.netbeans.lib.editor.util.CharSequenceUtilities;

/**
 * Various utility methods related to token text.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenUtilities {

    private TokenUtilities() {
        // no instances
    }

    /**
     * Test whether the given character sequences represent
     * the same text content.
     *
     * @param text1 non-null text to be compared to the other text parameter.
     * @param text2 non-null text to be compared to the previous text parameter.
     * @return <code>true</code> if the given character sequences represent
     *  the same text content.
     */
    public static boolean textEquals(CharSequence text1, CharSequence text2) {
        return CharSequenceUtilities.textEquals(text1, text2);
    }
    
    /**
     * Compare character sequence to another object.
     * The match is successful if the second object is a character sequence as well
     * and both character sequences contain the same characters (or if both objects are null).
     *
     * @param text character sequence being compared to the given object.
     *  It may be <code>null</code>.
     * @param o object to be compared to the character sequence.
     *  It may be <code>null</code>.
     * @return true if both parameters are null or both are non-null
     *  and they contain the same text.
     */
    public static boolean equals(CharSequence text, Object o) {
        return CharSequenceUtilities.equals(text, o);
    }
    
    /**
     * Implementation of {@link String#indexOf(int)} for character sequences.
     */
    public static int indexOf(CharSequence text, int ch) {
        return CharSequenceUtilities.indexOf(text, ch);
    }

    /**
     * Implementation of {@link String#indexOf(int,int)} for character sequences.
     */
    public static int indexOf(CharSequence text, int ch, int fromIndex) {
        return CharSequenceUtilities.indexOf(text, ch, fromIndex);
    }
    
    /**
     * Implementation of {@link String#indexOf(String)} for character sequences.
     */
    public static int indexOf(CharSequence text, CharSequence seq) {
        return CharSequenceUtilities.indexOf(text, seq);
    }

    /**
     * Implementation of {@link String#indexOf(String,int)} for character sequences.
     */
    public static int indexOf(CharSequence text, CharSequence seq, int fromIndex) {
        return CharSequenceUtilities.indexOf(text, seq, fromIndex);
    }

    /**
     * Implementation of {@link String#lastIndexOf(String)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, CharSequence seq) {
        return CharSequenceUtilities.lastIndexOf(text, seq);
    }
    
    /**
     * Implementation of {@link String#lastIndexOf(String,int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, CharSequence seq, int fromIndex) {
        return CharSequenceUtilities.lastIndexOf(text, seq, fromIndex);
    }

    /**
     * Implementation of {@link String#lastIndexOf(int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, int ch) {
	return CharSequenceUtilities.lastIndexOf(text, ch);
    }

    /**
     * Implementation of {@link String#lastIndexOf(int,int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, int ch, int fromIndex) {
        return CharSequenceUtilities.lastIndexOf(text, ch, fromIndex);
    }

    /**
     * Implementation of {@link String#startsWith(String)} for character sequences.
     */
    public static boolean startsWith(CharSequence text, CharSequence prefix) {
        return CharSequenceUtilities.startsWith(text, prefix);
    }
    
    /**
     * Implementation of {@link String#endsWith(String)} for character sequences.
     */
    public static boolean endsWith(CharSequence text, CharSequence suffix) {
        return CharSequenceUtilities.endsWith(text, suffix);
    }

    /**
     * Implementation of {@link String#trim()} for character sequences.
     */
    public static CharSequence trim(CharSequence text) {
        return CharSequenceUtilities.trim(text);
    }

    /**
     * Return the given text as String
     * translating the special characters (and '\') into escape sequences.
     *
     * @param text non-null text to be debugged.
     * @return non-null string containing the debug text.
     */
    public static String debugText(CharSequence text) {
        return CharSequenceUtilities.debugText(text);
    }

}
