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

package org.netbeans.lib.lexer.inc;

import org.netbeans.lib.editor.util.AbstractCharSequence;

/**
 * Character sequence emulating state of a mutable input source
 * before the last modification.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class OriginalText extends AbstractCharSequence.StringLike {

    private final CharSequence currentText;

    private final int offset;

    private final int insertedTextLength;

    private final CharSequence removedText;

    private final int origLength;

    public OriginalText(CharSequence currentText, int offset, CharSequence removedText, int insertedTextLength) {
        this.currentText = currentText;
        this.offset = offset;
        this.removedText = (removedText != null) ? removedText : ""; // always non-null
        this.insertedTextLength = insertedTextLength;

        this.origLength = currentText.length() - insertedTextLength + this.removedText.length();
    }

    public int length() {
        return origLength;
    }

    public char charAt(int index) {
        if (index < offset) {
            return currentText.charAt(index);
        }
        index -= offset;
        if (index < removedText.length()) {
            return removedText.charAt(index);
        }
        return currentText.charAt(offset + index - removedText.length() + insertedTextLength);
    }

    public char[] toCharArray(int start, int end) {
        char[] chars = new char[end - start];
        int charsIndex = 0;
        if (start < offset) {
            int bound = (end < offset) ? end : offset;
            while (start < bound) {
                chars[charsIndex++] = currentText.charAt(start++);
            }
            if (end == bound) {
                return chars;
            }
        }
        start -= offset;
        end -= offset;
        int bound = removedText.length();
        if (start < bound) {
            if (end < bound) {
                bound = end;
            }
            while (start < bound) {
                chars[charsIndex++] = removedText.charAt(start++);
            }
            if (end == bound) {
                return chars;
            }
        }
        bound = offset - removedText.length() + insertedTextLength;
        start += bound;
        bound += end;
        while (start < bound) {
            chars[charsIndex++] = currentText.charAt(start++);
        }
        return chars;
    }

}
