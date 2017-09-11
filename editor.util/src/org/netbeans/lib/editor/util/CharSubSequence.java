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

package org.netbeans.lib.editor.util;

/**
 * Subsequence of the given character sequence. The backing sequence
 * is considered to be stable i.e. does not change length or content over time.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class CharSubSequence extends AbstractCharSequence {

    /**
     * Ensure that the given start and end parameters are valid indices
     * of the given text.
     * @throws IndexOutOfBoundsException if the start or end are not within bounds
     *  of the given text.
     * @deprecated use {@link CharSequenceUtilities#checkIndexesValid(CharSequence, int, int)}
     */
    public static void checkIndexesValid(CharSequence text, int start, int end) {
        CharSequenceUtilities.checkIndexesValid(text, start, end);
    }
    
    private int length;
    
    private int start;

    private CharSequence backingSequence;
    
    /**
     * Construct character subsequence with the given backing character sequence.
     *
     * @param backingSequence non-null backing character sequence. It is considered
     * to be stable and not to change over time.
     * @param start &gt;=0 starting index of the subsequence within
     *  the backing character sequence.
     * @param end &gt;= ending index of the subsequence within
     *  the backing character sequence.
     * @throws IndexOutOfBoundsException if the start or end are not within bounds
     *  of backingSequence.
     */
    public CharSubSequence(CharSequence backingSequence, int start, int end) {
        checkIndexesValid(backingSequence, start, end);
        this.backingSequence = backingSequence;
        this.start = start;
        this.length = end - start;
    }
    
    protected CharSequence backingSequence() {
        return backingSequence;
    }
    
    protected int start() {
        return start;
    }

    public int length() {
        return length;
    }

    public char charAt(int index) {
        CharSequenceUtilities.checkIndexValid(index, length);
        return backingSequence.charAt(start() + index);
    }

    /**
     * Subclass providing string-like implementation
     * of <code>hashCode()</code> and <code>equals()</code>
     * method accepting strings with the same content
     * like charsequence has.
     * <br>
     * This makes the class suitable for matching to strings
     * e.g. in maps.
     * <br>
     * <b>NOTE</b>: Matching is just uni-directional
     * i.e. charsequence.equals(string) works
     * but string.equals(charsequence) does not.
     */
    public static class StringLike extends CharSubSequence {

        public StringLike(CharSequence backingSequence, int start, int end) {
            super(backingSequence, start, end);
        }
    
        public int hashCode() {
            return CharSequenceUtilities.stringLikeHashCode(this);
        }

        public boolean equals(Object o) {
            return CharSequenceUtilities.equals(this, o);
        }
        
    }

}
