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
package org.netbeans.modules.spellchecker.plain;

import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class PlainTokenList implements TokenList {

    private final Document doc;
    private String currentWord;
    private int currentStartOffset;
    private int nextSearchOffset;
    private boolean hidden;

    /** Creates a new instance of JavaTokenList */
    public PlainTokenList(Document doc) {
        this.doc = doc;
    }

    
    public void setStartOffset(int offset) {
        currentWord = null;
        currentStartOffset = (-1);
        CharSequence content = DocumentUtilities.getText(doc);

        while (offset > 0 && offset < content.length()) {
            if (!Character.isLetter(content.charAt(offset))) {
                break;
            }
            
            offset--;
        }
        
        this.nextSearchOffset = offset;
        FileObject fileObject = FileUtil.getConfigFile ("Spellcheckers/Plain");
        Boolean b = (Boolean) fileObject.getAttribute ("Hidden");
        hidden = Boolean.TRUE.equals (b);
    }

    public int getCurrentWordStartOffset() {
        return currentStartOffset;
    }

    public CharSequence getCurrentWordText() {
        return currentWord;
    }

    public boolean nextWord() {
        if (hidden) return false;
        try {
            int offset = nextSearchOffset;
            boolean searching = true;
            CharSequence content = DocumentUtilities.getText(doc);

            while (offset < content.length()) {
                char c = content.charAt(offset);

                if (searching) {
                    if (Character.isLetter(c)) {
                        searching = false;
                        currentStartOffset = offset;
                    }
                } else {
                    if (!Character.isLetter(c)) {
                        nextSearchOffset = offset;
                        currentWord = doc.getText(currentStartOffset, offset - currentStartOffset);
                        return true;
                    }
                }
                
                offset++;
            }

            nextSearchOffset = doc.getLength();

            if (searching) {
                return false;
            }
            currentWord = doc.getText(currentStartOffset, doc.getLength() - currentStartOffset);

            return true;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

}
