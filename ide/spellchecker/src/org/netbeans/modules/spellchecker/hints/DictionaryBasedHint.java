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
package org.netbeans.modules.spellchecker.hints;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public final class DictionaryBasedHint implements EnhancedFix {

    private String original;
    private Document doc;
    private String proposal;
    private Position[] span;
    private String sortText;
    
    public DictionaryBasedHint(String original, String proposal, Document doc, Position[] span, String sortText) {
        this.original = original;
        this.doc = doc;
        this.proposal = proposal;
        this.span = span;
        this.sortText = sortText;
    }

    public String getText() {
        return NbBundle.getMessage(DictionaryBasedHint.class, "FIX_ChangeWord", original, proposal);
    }

    public ChangeInfo implement() {
        try {
            NbDocument.runAtomicAsUser((StyledDocument) doc, new Runnable() {
                public void run() {
                    try {
                        doc.remove(span[0].getOffset(), span[1].getOffset() - span[0].getOffset());
                        doc.insertString(span[0].getOffset(), proposal, null);
                    } catch (BadLocationException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            });
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
	
	return null;
    }

    public CharSequence getSortText() {
        return sortText;
    }

}
