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

package org.netbeans.lib.lexer.token;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;

/**
 * Char sequence over join token parts.
 * 
 * @author Miloslav Metelka
 */

public final class JoinTokenText<T extends TokenId> implements CharSequence {
    
    private static final Logger LOG = Logger.getLogger(JoinTokenText.class.getName());

    private final List<PartToken<T>> joinedParts;

    private final int length;
    
    private int activePartIndex;
    
    private CharSequence activeInputText;

    private int activeStartCharIndex;
    
    private int activeEndCharIndex;
    
    public JoinTokenText(List<PartToken<T>> joinedParts, int length) {
        this.joinedParts = joinedParts;
        this.activeInputText = joinedParts.get(0).text();
        // Implicit: this.activeStartCharIndex = 0;
        this.activeEndCharIndex = activeInputText.length();
        this.length = length;
    }

    @Override
    public synchronized char charAt(int index) {
        if (index < activeStartCharIndex) { // Find non-empty previous
            if (index < 0)
                throw new IndexOutOfBoundsException("index=" + index + " < 0");
            do {
                activePartIndex--;
                if (activePartIndex < 0) { // Should never happen
                    LOG.log(Level.WARNING, "Internal error: index=" + index + ", " + dumpState());
                }
                activeInputText = joinedParts.get(activePartIndex).text();
                int len = activeInputText.length();
                activeEndCharIndex = activeStartCharIndex;
                activeStartCharIndex -= len;
            } while (index < activeStartCharIndex);
        } else if (index >= activeEndCharIndex) { // Find non-empty next
            if (index >= length)
                throw new IndexOutOfBoundsException("index=" + index + " >= length()=" + length);
            do {
                activePartIndex++;
                activeInputText = joinedParts.get(activePartIndex).text();
                int len = activeInputText.length();
                activeStartCharIndex = activeEndCharIndex;
                activeEndCharIndex += len;
            } while (index >= activeEndCharIndex);
        }

        // Valid char within current segment
        return activeInputText.charAt(index - activeStartCharIndex);
    }

    @Override
    public int length() {
        return length;
    }
    
    @Override
    public CharSequence subSequence(int start, int end) {
        return CharSequenceUtilities.toString(this, start, end);
    }
    
    @Override
    public synchronized String toString() {
        return CharSequenceUtilities.toString(this);
    }
    
    private String dumpState() {
        return "activeTokenListIndex=" + activePartIndex +
                ", activeStartCharIndex=" + activeStartCharIndex +
                ", activeEndCharIndex=" + activeEndCharIndex +
                ", length=" + length;
    }

}
