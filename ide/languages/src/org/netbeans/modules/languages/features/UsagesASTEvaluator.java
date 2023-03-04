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

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.EditorStyleConstants;
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
import org.netbeans.api.languages.database.DatabaseItem;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.modules.languages.parser.SyntaxError;


/**
 *
 * @author hanz
 */
class UsagesASTEvaluator extends ASTEvaluator {

    private static Map<Document,WeakReference<UsagesASTEvaluator>> cache = new WeakHashMap<Document,WeakReference<UsagesASTEvaluator>> ();
    
    static void register (Document document) {
        if (get (document) != null) return;
        cache.put (document, new WeakReference<UsagesASTEvaluator> (new UsagesASTEvaluator (document)));
    }
    
    static void unregister (Document document) {
        UsagesASTEvaluator evaluator = get (document);
        if (evaluator != null)
            ParserManager.get (document).removeASTEvaluator (evaluator);
        cache.remove (document);
    }
    
    static UsagesASTEvaluator get (Document document) {
        WeakReference<UsagesASTEvaluator> weakReference = cache.get (document);
        if (weakReference == null) return null;
        return weakReference.get ();
    }
    
    static void addDatabaseDefinition (Document document, DatabaseDefinition definition) {
        UsagesASTEvaluator evaluator = get (document);
        evaluator.definitions.add (definition);
    }

    
    private Document                    document;
    private ParserManagerImpl           parserManager;
    private Set<DatabaseDefinition>     definitions;
    
    
    UsagesASTEvaluator (Document document) {
        this.document = document;
        parserManager = (ParserManagerImpl) ParserManager.get (document);        
        parserManager.addASTEvaluator (this);
    }
    
    private List<DatabaseItem> unresolvedUsages;
    private List<Boolean> unresolvedUsages_declaration_precedes_ussage;

    public void beforeEvaluation (State state, ASTNode root) {
        unresolvedUsages = null;
        definitions = new HashSet<DatabaseDefinition> ();
    }

    public void afterEvaluation (State state, ASTNode root) {
        if (unresolvedUsages != null) {
            Iterator<DatabaseItem> it = unresolvedUsages.iterator ();
            Iterator<Boolean> it2 = unresolvedUsages_declaration_precedes_ussage.iterator ();
            while (it.hasNext ()) {
                if (parserManager != null && parserManager.getState () == State.PARSING)
                    return;
                DatabaseUsage usage = (DatabaseUsage) it.next ();
                DatabaseContext context = (DatabaseContext) it.next ();
                boolean declaration_precedes_ussage = it2.next ();
                DatabaseDefinition definition = context.getDefinition (
                    usage.getName (), 
                    usage.getOffset ()
                );
                //S ystem.out.println("add2 " + usage + " (" + definition + ") to " + context);
                if (definition != null) {
                    if (declaration_precedes_ussage && definition.getOffset () > usage.getOffset ()) continue;
                    definition.addUsage (usage);
                    context.addUsage (usage);
                    usage.setDatabaseDefinition (definition);
                    highlightUssage (usage, definition);
                    if (definitions.contains (definition)) {
                        highlightDefinition (definition);
                        definitions.remove (definition);
                    }
                } else {
                    highlightUnresolvedUssage (usage);
                }
            }
            unresolvedUsages = null;
        }
        Iterator<DatabaseDefinition> it2 = definitions.iterator ();
        while (it2.hasNext ())
            highlightUnusedDefinition (it2.next ());
         Iterator<SyntaxError> it3 = parserManager.getSyntaxErrors ().iterator ();
         while (it3.hasNext ()) {
            SyntaxError syntaxError = it3.next ();
            highlightSyntaxError (syntaxError, root);
         }
         SemanticHighlightsLayer.update (document);
    }

    public void evaluate (State state, List<ASTItem> path, Feature feature) {
        SyntaxContext sc = SyntaxContext.create (document, ASTPath.create (path));
        if (!feature.getBoolean ("condition", sc, true)) return;
        ASTItem leaf = path.get (path.size () - 1);
        DatabaseContext context = ContextASTEvaluator.getCurrentContext (document, leaf.getOffset ());
        String name = ((String) feature.getValue ("name", sc)).trim ();
        boolean declaration_precedes_ussage = feature.getBoolean ("declaration_precedes_usage", true);
        DatabaseDefinition definition = context.getDefinition (name, leaf.getOffset ());
        if (definition != null && definition.getOffset () == leaf.getOffset ()) return;
        if (definition != null && declaration_precedes_ussage && definition.getOffset () > leaf.getOffset ()) return;
        DatabaseUsage usage = new DatabaseUsage (name, leaf.getOffset (), leaf.getEndOffset ());
//        S ystem.out.println("add " + usage + " (" + definition + ") to " + context);
        if (definition != null) {
            definition.addUsage (usage);
            usage.setDatabaseDefinition (definition);
            context.addUsage (usage);
            highlightUssage (usage, definition);
            if (definitions.contains (definition)) {
                highlightDefinition (definition);
                definitions.remove (definition);
            }
        } else {
            if (unresolvedUsages == null) {
                unresolvedUsages = new ArrayList<DatabaseItem> ();
                unresolvedUsages_declaration_precedes_ussage = new ArrayList<Boolean> ();
            }
            unresolvedUsages.add (usage);
            unresolvedUsages.add (context);
            unresolvedUsages_declaration_precedes_ussage.add (declaration_precedes_ussage);
        }
        ContextASTEvaluator.setEvaluated(document, true);
    }

    public String getFeatureName () {
        return "SEMANTIC_USAGE";
    }
    
    private void highlightUssage (DatabaseUsage usage, DatabaseDefinition definition) {
        if ("parameter".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                usage.getOffset (), 
                usage.getEndOffset (), 
                getParameterAttributes ()
            );
        else
        if ("local".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                usage.getOffset (), 
                usage.getEndOffset (), 
                getLocalVariableAttributes ()
            );
        else
        if ("field".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                usage.getOffset (), 
                usage.getEndOffset (), 
                getFieldAttributes ()
            );
    }
    
    private void highlightUnresolvedUssage (DatabaseUsage usage) {
//        SemanticHighlightsLayer.addHighlight (
//            document, 
//            usage.getOffset (), 
//            usage.getEndOffset (), 
//            getUnresolvedUssageAttributes ()
//        );
    }
    
    private void highlightSyntaxError (SyntaxError syntaxError, ASTNode root) {
        ASTItem item = syntaxError.getItem ();
        if (item.getLength () == 0) {
            int offset = item.getOffset ();
            if (offset >= root.getEndOffset ()) offset = root.getEndOffset () - 1;
            item = root.findPath (offset).getLeaf ();
        }
        SemanticHighlightsLayer.addHighlight (
            document, 
            item.getOffset (), 
            item.getEndOffset (), 
            getSyntaxErrorAttributes ()
        );
    }

    private void highlightDefinition (DatabaseDefinition definition) {
        if ("parameter".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                definition.getOffset (), 
                definition.getEndOffset (), 
                getParameterAttributes ()
            );
        else
        if ("variable".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                definition.getOffset (), 
                definition.getEndOffset (), 
                getLocalVariableAttributes ()
            );
        else
        if ("field".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                definition.getOffset (), 
                definition.getEndOffset (), 
                getFieldAttributes ()
            );
    }

    private void highlightUnusedDefinition (DatabaseDefinition definition) {
        if ("parameter".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                definition.getOffset (), 
                definition.getEndOffset (), 
                getUnusedParameterAttributes ()
            );
        else
        if ("field".equals (definition.getType ()))
            SemanticHighlightsLayer.addHighlight (
                document, 
                definition.getOffset (), 
                definition.getEndOffset (), 
                getUnusedFieldAttributes ()
            );
        else
            SemanticHighlightsLayer.addHighlight (
                document, 
                definition.getOffset (), 
                definition.getEndOffset (), 
                getUnusedLocalVariableAttributes ()
            );
    }
    
    private static AttributeSet unusedParameterAttributeSet;
    
    private static AttributeSet getUnusedParameterAttributes () {
        if (unusedParameterAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            sas.addAttribute (EditorStyleConstants.WaveUnderlineColor, new Color (153, 153, 153));
            unusedParameterAttributeSet = sas;
        }
        return unusedParameterAttributeSet;
    }
    
    private static AttributeSet syntaxErrorAttributeSet;
    
    private static AttributeSet getSyntaxErrorAttributes () {
        if (syntaxErrorAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            sas.addAttribute (EditorStyleConstants.WaveUnderlineColor, Color.red);
            syntaxErrorAttributeSet = sas;
        }
        return syntaxErrorAttributeSet;
    }
    
    private static AttributeSet parameterAttributeSet;
    
    private static AttributeSet getParameterAttributes () {
        if (parameterAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            parameterAttributeSet = sas;
        }
        return parameterAttributeSet;
    }
    
    private static AttributeSet unusedLocalVariableAttributeSet;
    
    private static AttributeSet getUnusedLocalVariableAttributes () {
        if (unusedLocalVariableAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            sas.addAttribute (EditorStyleConstants.WaveUnderlineColor, new Color (153, 153, 153));
            unusedLocalVariableAttributeSet = sas;
        }
        return unusedLocalVariableAttributeSet;
    }
    
    private static AttributeSet unresolvedUssageAttributeSet;
    
    private static AttributeSet getUnresolvedUssageAttributes () {
        if (unresolvedUssageAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            sas.addAttribute (EditorStyleConstants.WaveUnderlineColor, Color.red);
            unresolvedUssageAttributeSet = sas;
        }
        return unresolvedUssageAttributeSet;
    }
    
    private static AttributeSet localVariableAttributeSet;
    
    private static AttributeSet getLocalVariableAttributes () {
        if (localVariableAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            localVariableAttributeSet = sas;
        }
        return localVariableAttributeSet;
    }
    
    private static AttributeSet unusedFieldAttributeSet;
    
    private static AttributeSet getUnusedFieldAttributes () {
        if (unusedFieldAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            sas.addAttribute (EditorStyleConstants.WaveUnderlineColor, new Color (153, 153, 153));
            StyleConstants.setForeground (sas, new Color (0, 153, 0));
            unusedFieldAttributeSet = sas;
        }
        return unusedFieldAttributeSet;
    }
    
    private static AttributeSet fieldAttributeSet;
    
    private static AttributeSet getFieldAttributes () {
        if (fieldAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            StyleConstants.setForeground (sas, new Color (0, 153, 0));
            fieldAttributeSet = sas;
        }
        return fieldAttributeSet;
    }
}
