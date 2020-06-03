/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
