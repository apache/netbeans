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

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.lexer.SLexer;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;


/**
 *
 * @author Jan Jancura
 */
class LanguagesHighlightsLayer extends AbstractHighlightsContainer {

    private Document document;
    
    LanguagesHighlightsLayer (Document document) {
        this.document = document;
    }

    public HighlightsSequence getHighlights (int startOffset, int endOffset) {
        TokenSequence seq = TokenHierarchy.get (document).tokenSequence();
        if (seq != null) {
            return new Highlights (seq, startOffset, endOffset); //NOI18N
        } else {
            return HighlightsSequence.EMPTY;
        }
    }

    
    private static class Highlights implements HighlightsSequence {

        private int                 endOffset;
        private int                 startOffset1;
        private int                 endOffset1;
        private SimpleAttributeSet  attributeSet;
        private TokenSequence       tokenSequence;
        private String              mimeType;
        
        
        private Highlights (TokenSequence tokenSequence, int startOffset, int endOffset) {
            this.tokenSequence = tokenSequence;
            this.mimeType = tokenSequence.language().mimeType();
            this.endOffset = endOffset;
            startOffset1 = startOffset;
            endOffset1 = startOffset;
        }
        
        public boolean moveNext () {
            if (tokenSequence == null) return false;
            attributeSet = new SimpleAttributeSet ();
            do {
                startOffset1 = endOffset1;
                mark (tokenSequence);
                if (endOffset1 > startOffset1) return true;
                tokenSequence.move (startOffset1);
                if (!tokenSequence.moveNext ()) return false;
                Token token = tokenSequence.token ();
                endOffset1 = tokenSequence.offset () + token.length ();
            } while (endOffset1 < endOffset);
            return false;
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

        private void mark (TokenSequence ts) {
            ts.move (startOffset1);
            if (!ts.moveNext ()) return;
            Token token = ts.token ();
            TokenSequence ts2 = ts.embedded ();
            if (ts2 == null) return;
            String mimeTypeOut = ts.language ().mimeType ();
            String mimeTypeIn = ts2.language ().mimeType ();
            if (token.id ().name ().equals (SLexer.EMBEDDING_TOKEN_TYPE_NAME)) {
                Color c = getPreprocessorImportsColor (mimeTypeIn);
                if (c != null) {
                    attributeSet.addAttribute (StyleConstants.Background, c);
                    attributeSet.addAttribute (HighlightsContainer.ATTR_EXTENDS_EOL, Boolean.TRUE);
                    endOffset1 = tokenSequence.offset () + token.length ();
                }
            } else
            if (!mimeTypeOut.equals (mimeTypeIn)) {
                Color c = getTokenImportsColor (mimeTypeOut, mimeTypeIn, token.id ().name ());
                if (c != null) {
                    attributeSet.addAttribute (StyleConstants.Background, c);
                    attributeSet.addAttribute (HighlightsContainer.ATTR_EXTENDS_EOL, Boolean.TRUE);
                    endOffset1 = tokenSequence.offset () + token.length ();
                }
            }
            mark (ts2);
        }

        private Map<String,Map<String,Color>> tokenImportColors = new HashMap<String,Map<String,Color>> ();

        private Color getPreprocessorImportsColor (String mimeTypeIn) {
            if (preprocessorImportColors == null) {
                preprocessorImportColors = new HashMap<String,Color> ();
                try {
                    Language l = LanguagesManager.getDefault ().
                        getLanguage (mimeType);
                    Feature properties = l.getPreprocessorImport ();
                    if (properties != null) {
                        String mimeType = (String) properties.getValue ("mimeType");
                        Color color = ColorsManager.readColor (
                            (String) properties.getValue ("background_color")
                        );
                        if (color != null)
                            preprocessorImportColors.put (mimeType, color);
                    }
                } catch (ParseException ex) {
                }
            }
            return preprocessorImportColors.get (mimeTypeIn);
        }

        private Map<String,Color> preprocessorImportColors;

        private Color getTokenImportsColor (String mimeTypeOut, String mimeTypeIn, String tokenTypeIn) {
            Map<String,Color> m = tokenImportColors.get (mimeTypeOut);
            if (m == null) {
                m = new HashMap<String,Color> ();
                tokenImportColors.put (mimeTypeOut, m);
                try {
                    Language l = LanguagesManager.getDefault ().
                        getLanguage (mimeTypeOut);
                    Map<String,Feature> m2 = l.getTokenImports ();
                    Iterator<String> it = m2.keySet ().iterator ();
                    while (it.hasNext ()) {
                        String tokenType = it.next ();
                        Feature properties = m2.get (tokenType);
                        Color color = ColorsManager.readColor (
                            (String) properties.getValue ("background_color")
                        );
                        if (color != null)
                            m.put (tokenType, color);
                    }
                } catch (LanguageDefinitionNotFoundException ex) {
                }
            }
            if (m.containsKey (tokenTypeIn))
                return m.get (tokenTypeIn);
            return m.get (mimeTypeIn);
        }
    }
}
