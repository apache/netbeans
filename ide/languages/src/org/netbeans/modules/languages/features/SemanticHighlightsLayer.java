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
package org.netbeans.modules.languages.features;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;

import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;


/**
 *
 * @author Jan Jancura
 */
class SemanticHighlightsLayer extends AbstractHighlightsContainer {

    private static Map<Document,List<WeakReference<SemanticHighlightsLayer>>> cache = new WeakHashMap<Document,List<WeakReference<SemanticHighlightsLayer>>> ();

    static synchronized void addHighlight (
        Document document, 
        int startOffset,
        int endOffset,
        AttributeSet attributeSet
    ) {
        List<WeakReference<SemanticHighlightsLayer>> layers = cache.get (document);
        List<WeakReference<SemanticHighlightsLayer>> newLayers = new ArrayList<WeakReference<SemanticHighlightsLayer>> ();
        boolean remove = true;
        if (layers != null) {
            Iterator<WeakReference<SemanticHighlightsLayer>> it = layers.iterator ();
            while (it.hasNext()) {
                WeakReference<SemanticHighlightsLayer> weakReference = it.next ();
                SemanticHighlightsLayer layer = weakReference.get ();
                if (layer == null) continue;
                remove = false;
                synchronized (layer) {
                    if (layer.offsetsBag1 == null)
                        layer.offsetsBag1 = new OffsetsBag (document);
                    layer.offsetsBag1.addHighlight (startOffset, endOffset, attributeSet);
                }
                newLayers.add (weakReference);
            }
        }
        if (remove) {
            cache.remove (document);
            ColorsASTEvaluator.unregister (document);
            DeclarationASTEvaluator.unregister (document);
            ContextASTEvaluator.unregister (document);
            UsagesASTEvaluator.unregister (document);
        } else
            cache.put (document, newLayers);
    }
    
    static synchronized void update (Document document) {
        List<WeakReference<SemanticHighlightsLayer>> layers = cache.get (document);
        boolean remove = true;
        if (layers != null) {
            Iterator<WeakReference<SemanticHighlightsLayer>> it = layers.iterator ();
            while (it.hasNext()) {
                WeakReference<SemanticHighlightsLayer> weakReference = it.next ();
                SemanticHighlightsLayer layer = weakReference.get ();
                if (layer == null) continue;
                remove = false;
                synchronized (layer) {
                    layer.offsetsBag = layer.offsetsBag1;
                    layer.offsetsBag1 = null;
                    if (layer.offsetsBag == null)
                        layer.offsetsBag = new OffsetsBag (document);
                }
                layer.fireHighlightsChange (0, document.getLength ());
            }
        }
        if (remove) {
            cache.remove (document);
            ColorsASTEvaluator.unregister (document);
            DeclarationASTEvaluator.unregister (document);
            ContextASTEvaluator.unregister (document);
            UsagesASTEvaluator.unregister (document);
        }
    }

    
    private Document            document;
    private OffsetsBag          offsetsBag;
    private OffsetsBag          offsetsBag1;
    
    SemanticHighlightsLayer (Document document) {
        this.document = document;
        ColorsASTEvaluator.register (document);
        DeclarationASTEvaluator.register (document);
        ContextASTEvaluator.register (document);
        UsagesASTEvaluator.register (document);
        
        synchronized(SemanticHighlightsLayer.class) {
            List<WeakReference<SemanticHighlightsLayer>> layers = cache.get (document);
            if (layers == null) {
                layers = new ArrayList<WeakReference<SemanticHighlightsLayer>> ();
                cache.put (document, layers);
            }
            layers.add (new WeakReference<SemanticHighlightsLayer> (this));
        }
    }
    
    public synchronized HighlightsSequence getHighlights (int startOffset, int endOffset) {
                                                                                //S ystem.out.println("SemanticHighlightsLayer.getHighlights " + startOffset + " : " + endOffset);
        if (offsetsBag == null) {
            offsetsBag = new OffsetsBag (document);
            refresh ();
        }
        return offsetsBag.getHighlights (startOffset, endOffset);
    }
    
    private void refresh () {
        ParserManagerImpl parserManager = ParserManagerImpl.getImpl (document);
        ASTNode root = parserManager.getAST ();
        if (root == null) return;
        parserManager.fire (
            parserManager.getState (), 
            null, 
            getEvaluators (), 
            root
        );
    }
    
    private Map<String,Set<ASTEvaluator>> evaluators;
    
    private Map<String,Set<ASTEvaluator>> getEvaluators () {
        if (evaluators == null) {
            evaluators = new HashMap<String,Set<ASTEvaluator>> ();
            ColorsASTEvaluator colorsASTEvaluator = ColorsASTEvaluator.get (document);
            if (colorsASTEvaluator != null) {
                evaluators.put (colorsASTEvaluator.getFeatureName (), Collections.<ASTEvaluator>singleton (colorsASTEvaluator));
            }
            UsagesASTEvaluator usagesASTEvaluator = UsagesASTEvaluator.get (document);
            if (usagesASTEvaluator != null) {
                evaluators.put (usagesASTEvaluator.getFeatureName (), Collections.<ASTEvaluator>singleton (usagesASTEvaluator));
            }
            DeclarationASTEvaluator declarationASTEvaluator = DeclarationASTEvaluator.get (document);
            if (declarationASTEvaluator != null) {
                evaluators.put (declarationASTEvaluator.getFeatureName (), Collections.<ASTEvaluator>singleton (declarationASTEvaluator));
            }    
            ContextASTEvaluator contextASTEvaluator = ContextASTEvaluator.get (document);
            if (contextASTEvaluator != null) {
                evaluators.put (contextASTEvaluator.getFeatureName (), Collections.<ASTEvaluator>singleton (contextASTEvaluator));
            }
        }
        return evaluators;
    }
}
