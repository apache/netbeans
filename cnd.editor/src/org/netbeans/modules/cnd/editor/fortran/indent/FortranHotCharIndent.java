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

package org.netbeans.modules.cnd.editor.fortran.indent;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;

/**
 *
 */
public enum FortranHotCharIndent {
    INSTANCE;

    public boolean getKeywordBasedReformatBlock(BaseDocument doc, int dotPos, String typedText) {
        /* Check whether the user has written the ending 'e'
         * of the first 'else' on the line.
         */
        if ("e".equals(typedText) || "E".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "else")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "endsubroutine")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "end subroutine")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "end while")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "end type")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "endtype")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "case")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if ("o".equals(typedText) || "O".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "enddo")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "end do")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if ("f".equals(typedText) || "F".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "endif")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "end if")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "elseif")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "else if")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if ("m".equals(typedText) || "M".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "endprogram")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "end program")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "endenum")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "end enum")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if ("n".equals(typedText) || "N".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "end function")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "endfunction")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if ("l".equals(typedText) || "L".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "endforall")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "end forall")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if ("p".equals(typedText) || "P".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "endmap")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "end map")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if ("a".equals(typedText) || "A".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "endblockdata")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "end blockdata")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "end block data")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "endblock data")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if ("t".equals(typedText) || "T".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "endselect")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "end select")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        }
        if (typedText != null && typedText.length() == 1 && Character.isLetter(typedText.charAt(0))) {
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

    private boolean checkCase(BaseDocument doc, int fnw, String what) throws BadLocationException {
        return fnw >= 0 && fnw + what.length() <= doc.getLength() && what.equalsIgnoreCase(doc.getText(fnw, what.length()));
    }
}
