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
import javax.swing.text.Document;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.database.DatabaseContext;
import org.netbeans.api.languages.database.DatabaseUsage;
import org.netbeans.api.languages.database.DatabaseDefinition;
import org.netbeans.modules.languages.Feature;

/**
 *
 * @author hanz
 */
public class DeclarationASTEvaluator extends ASTEvaluator {

    private static Map<Document,WeakReference<DeclarationASTEvaluator>> cache = new WeakHashMap<Document,WeakReference<DeclarationASTEvaluator>> ();
    
    static void register (Document document) {
        if (get (document) != null) return;
        cache.put (document, new WeakReference<DeclarationASTEvaluator> (new DeclarationASTEvaluator (document)));
    }
    
    static void unregister (Document document) {
        DeclarationASTEvaluator evaluator = get (document);
        if (evaluator != null)
            ParserManager.get (document).removeASTEvaluator (evaluator);
        cache.remove (document);
    }
    
    static DeclarationASTEvaluator get (Document document) {
        WeakReference<DeclarationASTEvaluator> weakReference = cache.get (document);
        if (weakReference == null) return null;
        return weakReference.get ();
    }
    
    
    private Document            document;
    
    DeclarationASTEvaluator (Document document) {
        this.document = document;
        ParserManager.get (document).addASTEvaluator (this);
    }

    public void beforeEvaluation (State state, ASTNode root) {
    }

    public void afterEvaluation (State state, ASTNode root) {
    }

    public void evaluate (State state, List<ASTItem> path, Feature feature) {
        SyntaxContext sc = SyntaxContext.create (document, ASTPath.create (path));
        if (!feature.getBoolean ("condition", sc, true)) return;
        String name = ((String) feature.getValue ("name", sc)).trim ();
        String type = (String) feature.getValue ("type", sc);
        if (name != null && name.length() > 0) {
            String local = (String) feature.getValue ("local", sc);
            ASTItem leaf = path.get (path.size () - 1);
            DatabaseContext context = ContextASTEvaluator.getCurrentContext (document, leaf.getOffset ());
            if (local != null) {
                DatabaseContext c = context;
                while (c != null && !local.equals (c.getType ()))
                    c = c.getParent ();
                if (c != null) 
                    type = "local";
            }
            DatabaseContext con = context;
            if ("method".equals (type)) { // NOI18N
                con = con.getParent();
                if (con == null) {
                    con = context;
                }
            }
            DatabaseDefinition original = con.getDefinition (name, leaf.getOffset ());
            if (original != null) {
                original.addUsage (new DatabaseUsage(name, leaf.getOffset (), leaf.getEndOffset ()));
            } else {
                DatabaseDefinition definition = new DatabaseDefinition (name, type, leaf.getOffset (), leaf.getEndOffset ());
                con.addDefinition (definition);
                //S ystem.out.println("add " + definition + " to " + con);
                UsagesASTEvaluator.addDatabaseDefinition (document, definition);
            }
            ContextASTEvaluator.setEvaluated(document, true);
        }
    }

    public String getFeatureName () {
        return "SEMANTIC_DECLARATION";
    }
}

