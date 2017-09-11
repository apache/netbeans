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

package org.netbeans.modules.csl.api;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.ErrorManager;

/**
 * CamelCase operations - based on Java ones but rewritten to delegate all logic
 * to language plugins
 * 
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 * @author Tor Norbye
 */
/* package */ class CamelCaseOperations {

    static int nextCamelCasePosition(JTextComponent textComponent) {
        int offset = textComponent.getCaretPosition();
        Document doc = textComponent.getDocument();

        // Are we at the end of the document?
        if (offset == doc.getLength()) {
            return -1;
        }

        KeystrokeHandler bc = UiUtils.getBracketCompletion(doc, offset);
        if (bc != null) {
            int nextOffset = bc.getNextWordOffset(doc, offset, false);
            if (nextOffset != -1) {
                return nextOffset;
            }
        }
        
        try {
            return Utilities.getNextWord(textComponent, offset);
        } catch (BadLocationException ble) {
            // something went wrong :(
            ErrorManager.getDefault().notify(ble);
        }
        return -1;
    }

    static int previousCamelCasePosition(JTextComponent textComponent) {
        int offset = textComponent.getCaretPosition();

        // Are we at the beginning of the document?
        if (offset == 0) {
            return -1;
        }

        final Document doc = textComponent.getDocument();
        
        KeystrokeHandler bc = UiUtils.getBracketCompletion(doc, offset);
        if (bc != null) {
            int nextOffset = bc.getNextWordOffset(
                    doc, offset, true);
            if (nextOffset != -1) {
                return nextOffset;
            }
        }
        
        try {
            return Utilities.getPreviousWord(textComponent, offset);
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify(ble);
        }
        return -1;
    }

    static void replaceChar(JTextComponent textComponent, int offset, char c) {
        if (!textComponent.isEditable()) {
            return;
        }
        replaceText(textComponent, offset, 1, String.valueOf(c));
    }

    static void replaceText(JTextComponent textComponent, final int offset, final int length, final String text) {
        if (!textComponent.isEditable()) {
            return;
        }
        final Document document = textComponent.getDocument();
        Runnable r = new Runnable() {
            public @Override void run() {
                try {
                    if (length > 0) {
                        document.remove(offset, length);
                    }
                    document.insertString(offset, text, null);
                } catch (BadLocationException ble) {
                    ErrorManager.getDefault().notify(ble);
                }
            }
        };
        if (document instanceof BaseDocument) {
            ((BaseDocument)document).runAtomic(r);
        } else {
            r.run();
        }
    }
}
