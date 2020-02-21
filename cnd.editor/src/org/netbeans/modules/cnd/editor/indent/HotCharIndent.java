/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.indent;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.util.Exceptions;

/**
 *
 */
public enum HotCharIndent {
    INSTANCE;

    public boolean getKeywordBasedReformatBlock(BaseDocument doc, int dotPos, String typedText) {
        if (isTokenContinue(doc, dotPos)) {
            return false;
        }
        if ("e".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "else")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "#else")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if ("f".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "#if")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "#elif")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "#ifdef")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "#ifndef")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "#endif")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if (":".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "case")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "default")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "public")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "protected")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "private")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if ("{".equals(typedText) || "}".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                return checkCase(doc, fnw, typedText);
            } catch (BadLocationException e) {
            }
        }
        if (typedText != null &&
            typedText.length() == 1 && (Character.isLetter(typedText.charAt(0)) || typedText.charAt(0) == '#')) {
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, typedText+"\n") || // NOI18N
                    dotPos == doc.getLength()-1 && checkCase(doc, fnw, typedText)) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        }
        return false;
    }

    private static boolean checkCase(BaseDocument doc, int fnw, String what) throws BadLocationException {
        return fnw >= 0 && fnw + what.length() <= doc.getLength() && what.equals(doc.getText(fnw, what.length()));
    }
    
    static boolean isTokenContinue(BaseDocument doc, int dotPos) {
        try {
            int rowStart = Utilities.getRowStart(doc, dotPos);
            if (rowStart > 1) {
                String prev = doc.getText(rowStart-2, 2);
                if ("\\\n".equals(prev)) { // NOI18N
                    return true;
                }
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }
}
