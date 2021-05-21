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
package org.netbeans.modules.java.editor.semantic;

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.editor.settings.AttributesUtilities;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes;
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes.Coloring;
import org.netbeans.modules.java.editor.base.semantic.SemanticHighlighterBase;
import org.netbeans.modules.java.editor.base.semantic.SemanticHighlighterBase.ErrorDescriptionSetter;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.loaders.DataObject;
import org.openide.util.Pair;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class SemanticHighlighter extends SemanticHighlighterBase {

    protected boolean process(final CompilationInfo info, final Document doc) {
        long start = System.currentTimeMillis();
        boolean ret = process(info, doc, ERROR_DESCRIPTION_SETTER);
        Logger.getLogger("TIMER").log(Level.FINE, "Semantic",
            new Object[] {NbEditorUtilities.getFileObject(doc), System.currentTimeMillis() - start});
        return ret;
    }
    
    static ErrorDescriptionSetter ERROR_DESCRIPTION_SETTER = new ErrorDescriptionSetter() {
        
        public void setErrors(Document doc, List<ErrorDescription> errors, List<TreePathHandle> allUnusedImports) {}
        
        public void setHighlights(final Document doc, final Collection<Pair<int[], Coloring>> highlights, Map<int[], String> preText) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    OffsetsBag bag = new OffsetsBag(doc);
                    for (Pair<int[], Coloring> highlight : highlights) {
                        bag.addHighlight(highlight.first()[0], highlight.first()[1], ColoringManager.getColoringImpl(highlight.second()));
                    }
                    getImportHighlightsBag(doc).setHighlights(bag);
                    
                    OffsetsBag preTextBag = new OffsetsBag(doc);
                    for (Entry<int[], String> e : preText.entrySet()) {
                        preTextBag.addHighlight(e.getKey()[0], e.getKey()[1], AttributesUtilities.createImmutable("virtual-text-prepend", e.getValue()));
                    }
                    getPreTextBag(doc).setHighlights(preTextBag);
                }
            });
        }
    
        public void setColorings(final Document doc, final Map<Token, Coloring> colorings) {
            SwingUtilities.invokeLater(new Runnable () {
                public void run() {
                    Map<Token, Coloring> oldColors = LexerBasedHighlightLayer.getLayer(SemanticHighlighter.class, doc).getColorings();
                    Map<Token, Coloring> removedTokens = new IdentityHashMap<>(oldColors);
                    Set<Token> addedTokens = new HashSet<>();
                    for (Map.Entry<Token, Coloring> entrySet : colorings.entrySet()) {
                        Token t = entrySet.getKey();
                        Coloring oldColoring = removedTokens.remove(t);
                        if (oldColoring == null || !oldColoring.equals(entrySet.getValue())) {
                            addedTokens.add(t);
                        }
                    }
                    LexerBasedHighlightLayer.getLayer(SemanticHighlighter.class, doc).setColorings(colorings, addedTokens, removedTokens.keySet());
                }                
            });            
        }
    };

    private static final Object KEY_UNUSED_IMPORTS = new Object();
    static OffsetsBag getImportHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(KEY_UNUSED_IMPORTS);
        
        if (bag == null) {
            doc.putProperty(KEY_UNUSED_IMPORTS, bag = new OffsetsBag(doc));
            
            Object stream = doc.getProperty(Document.StreamDescriptionProperty);
            
            if (stream instanceof DataObject) {
//                TimesCollector.getDefault().reportReference(((DataObject) stream).getPrimaryFile(), "ImportsHighlightsBag", "[M] Imports Highlights Bag", bag);
            }
        }
        
        return bag;
    }

    private static final Object KEY_PRE_TEXT = new Object();
    static OffsetsBag getPreTextBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(KEY_PRE_TEXT);
        
        if (bag == null) {
            doc.putProperty(KEY_PRE_TEXT, bag = new OffsetsBag(doc));
            
            Object stream = doc.getProperty(Document.StreamDescriptionProperty);
            
            if (stream instanceof DataObject) {
//                TimesCollector.getDefault().reportReference(((DataObject) stream).getPrimaryFile(), "ImportsHighlightsBag", "[M] Imports Highlights Bag", bag);
            }
        }
        
        return bag;
    }

}
