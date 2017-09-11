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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages.features;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

import org.netbeans.api.languages.Highlighting;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;


/**
 *
 * @author Jan Jancura
 */
class TokenHighlightsLayer extends AbstractHighlightsContainer {

    private Highlighting            highlighting;
    private TokenHierarchy          hierarchy;
    private Document                document;
    private final PropertyChangeListener listener = new PropertyChangeListener () {
        private boolean parserInited;

        public void propertyChange (final PropertyChangeEvent evt) {
            if (!parserInited) {
                String mimeType = (String) document.getProperty ("mimeType");
                try {
                    Language language = LanguagesManager.getDefault().getLanguage(mimeType);
                    if (language.getParser() != null) {
                        parserInited = true;
                    }
                } catch (LanguageDefinitionNotFoundException ex) {
                }
            }
            if (parserInited) {
                fireHighlightsChange ((Integer) evt.getOldValue (), (Integer) evt.getNewValue ());
            }
        }            
    };
    
    TokenHighlightsLayer (final Document document) {
        highlighting = Highlighting.getHighlighting (document);
        hierarchy = TokenHierarchy.get (document);
        this.document = document;
        highlighting.addPropertyChangeListener (WeakListeners.propertyChange(listener, highlighting));
    }
    
    public HighlightsSequence getHighlights (int startOffset, int endOffset) {
        return new Highlights (document, highlighting, hierarchy, startOffset, endOffset);
    }
    
    
    private static class Highlights implements HighlightsSequence {

        private int                 endOffset;
        private int                 startOffset1;
        private int                 endOffset1;
        private SimpleAttributeSet  attributeSet;
        private Highlighting        highlighting;
        private TokenHierarchy      hierarchy;
        private Document            document;
        
        private Highlights (
            Document                document,
            Highlighting            highlighting, 
            TokenHierarchy          hierarchy, 
            int                     startOffset, 
            int                     endOffset
        ) {
            this.document =         document;
            this.endOffset =        endOffset;
            this.highlighting =     highlighting;
            this.hierarchy =        hierarchy;
            endOffset1 =            startOffset;
        }
        
        public boolean moveNext () {
            attributeSet = new SimpleAttributeSet ();
            startOffset1 = endOffset1;
            if (hierarchy == null) return false;
            TokenSequence ts = hierarchy.tokenSequence ();
            if (ts == null) return false;
            moveNext(ts);
            return endOffset1 > startOffset1 && startOffset1 < endOffset;
        }

        private void moveNext(TokenSequence ts) {
            AttributeSet as = null;
            do {
                ts.move(startOffset1);
                if (!ts.moveNext()) {
                    return;
                }
                Token t = ts.token();
                if (ts.language() == null)
                    throw new NullPointerException ("ts.language()==null: TS " + ts + " : " + document.getProperty("mimeType"));
                as = highlighting.get (ts.offset(), ts.offset() + t.length());
                if (as != null) {
                    attributeSet.addAttributes(as);
                    endOffset1 = ts.offset() + t.length();
                    return;
                }
                TokenSequence ts1 = ts.embedded();
                if (ts1 != null) {
                    moveNext(ts1);
                }
                if (endOffset1 > startOffset1) {
                    return;
                }
                if (ts.token() != null) {
                    startOffset1 = ts.offset() + ts.token().length();
                    endOffset1 = startOffset1;
                } else {
                    return;
                }
            } while (startOffset1 < endOffset);
        }

        public int getStartOffset () {
            return startOffset1;
        }

        public int getEndOffset () {
            return endOffset1;
        }

        public AttributeSet getAttributes () {
            return attributeSet;
        }
    }
}
