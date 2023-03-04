/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
