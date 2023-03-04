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
import java.util.EmptyStackException;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.database.DatabaseContext;
import org.netbeans.modules.languages.Feature;


/**
 *
 * @author hanz
 */
public class ContextASTEvaluator extends ASTEvaluator {

    private static Map<Document,WeakReference<ContextASTEvaluator>> cache = new WeakHashMap<Document,WeakReference<ContextASTEvaluator>> ();
    
    static void register (Document document) {
        if (get (document) != null) return;
        cache.put (document, new WeakReference<ContextASTEvaluator> (new ContextASTEvaluator (document)));
    }
    
    static void unregister (Document document) {
        ContextASTEvaluator evaluator = get (document);
        if (evaluator != null)
            ParserManager.get (document).removeASTEvaluator (evaluator);
        cache.remove (document);
    }
    
    static ContextASTEvaluator get (Document document) {
        WeakReference<ContextASTEvaluator> weakReference = cache.get (document);
        if (weakReference == null) return null;
        return weakReference.get ();
    }
    
    static DatabaseContext getRootContext (Document document) {
        ContextASTEvaluator evaluator = get (document);
        if (evaluator == null) return null;
        return evaluator.rootContext;
    }
    
    static DatabaseContext getCurrentContext (Document document, int offset) {
        ContextASTEvaluator evaluator = get (document);
        try {
            if (evaluator == null) return null;
            DatabaseContext context = evaluator.currentContext.peek ();
            while (offset < context.getOffset () || context.getEndOffset () <= offset) {
                evaluator.currentContext.pop ();
                context = evaluator.currentContext.peek ();
            }
            return context;
        } catch (EmptyStackException ex) {
            return evaluator.rootContext;
        }
    }
    
    static void setEvaluated(Document document, boolean evaluated) {
        ContextASTEvaluator evaluator = get (document);
        if (evaluator == null) return;
        evaluator.evaluated = evaluated;
    }
    
    private Document                    document;
    private DatabaseContext             rootContext;
    private Stack<DatabaseContext>      currentContext;
    private boolean evaluated;
    
    
    ContextASTEvaluator (Document document) {
        this.document = document;
        ParserManager.get (document).addASTEvaluator (this);
    }

    public void beforeEvaluation (State state, ASTNode root) {
        rootContext = new DatabaseContext (null, null, root.getOffset (), root.getEndOffset ());
        currentContext = new Stack<DatabaseContext> ();
        currentContext.push (rootContext);
        evaluated = false;
    }

    public void afterEvaluation (State state, ASTNode root) {
        if (evaluated) {
            DatabaseManager.setRoot (root, rootContext);
        }
    }

    public void evaluate (State state, List<ASTItem> path, Feature feature) {
        SyntaxContext sc = SyntaxContext.create (document, ASTPath.create (path));
        if (!feature.getBoolean ("condition", sc, true)) return;
        String type = (String) feature.getValue ("type");
        ASTItem leaf = path.get (path.size () - 1);
        DatabaseContext context = getCurrentContext (document, leaf.getOffset ());
        DatabaseContext newContext = new DatabaseContext (context, type, leaf.getOffset (), leaf.getEndOffset ());
        context.addContext (leaf, newContext);
        currentContext.push (newContext);
        evaluated = true;
    }

    public String getFeatureName () {
        return "SEMANTIC_CONTEXT";
    }
}
