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
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.modules.languages.Feature;


/**
 *
 * @author hanz
 */
public class ColorsASTEvaluator extends ASTEvaluator {

    private static Map<Document,WeakReference<ColorsASTEvaluator>> cache = new WeakHashMap<Document,WeakReference<ColorsASTEvaluator>> ();
    
    static void register (Document document) {
        if (get (document) != null) return;
        cache.put (document, new WeakReference<ColorsASTEvaluator> (new ColorsASTEvaluator (document)));
    }
    
    static void unregister (Document document) {
        ColorsASTEvaluator evaluator = get (document);
        if (evaluator != null)
            ParserManager.get (document).removeASTEvaluator (evaluator);
        cache.remove (document);
    }
    
    static ColorsASTEvaluator get (Document document) {
        WeakReference<ColorsASTEvaluator> weakReference = cache.get (document);
        if (weakReference == null) return null;
        return weakReference.get ();
    }
    
    private Document document;

    private ColorsASTEvaluator (Document document) {
        this.document = document;
        ParserManager.get (document).addASTEvaluator (this);
    }

    public String getFeatureName () {
        return "COLOR";
    }

    public void beforeEvaluation (State state, ASTNode root) {
        if (state == State.PARSING) return;
    }

    public void afterEvaluation (State state, ASTNode root) {
    }

    public void evaluate (State state, List<ASTItem> path, Feature feature) {
        if (state == State.PARSING) return;
        AttributeSet attributeSet = null;
        ASTItem leaf = path.get (path.size () - 1);
        SyntaxContext context = SyntaxContext.create (document, ASTPath.create (path));
        if (feature.getBoolean ("condition", context, true)) {
            attributeSet = ColorsManager.createColoring (feature, null);
            SemanticHighlightsLayer.addHighlight (document, leaf.getOffset (), leaf.getEndOffset (), attributeSet);
        }
    }
}
