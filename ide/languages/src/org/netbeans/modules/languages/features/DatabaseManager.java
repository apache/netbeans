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

import java.util.Map;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.Document;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.database.DatabaseContext;
import org.netbeans.api.languages.database.DatabaseUsage;
import org.netbeans.api.languages.database.DatabaseDefinition;
import org.netbeans.api.languages.database.DatabaseItem;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;


/**
 *
 * @author Jan Jancura
 */
public class DatabaseManager {
    
    private NbEditorDocument            document;

    
    /** Creates a new instance of AnnotationManager */
    public DatabaseManager (Document document) {
        this.document = (NbEditorDocument) document;
    }
    
    static DatabaseContext parse (
        ASTNode ast, 
        Document doc,
        ParserManager parser
    ) {
        DatabaseContext rootContext = new DatabaseContext (null, null, ast.getOffset (), ast.getEndOffset ());
        List<ASTItem> path = new ArrayList<ASTItem> ();
        path.add (ast);
        List<DatabaseItem> unresolvedUsages = new ArrayList<DatabaseItem> ();
        process (path, rootContext, unresolvedUsages, doc, parser);
        Iterator<DatabaseItem> it2 = unresolvedUsages.iterator ();
        while (it2.hasNext ()) {
            if (parser != null && parser.getState () == State.PARSING)
                return null;
            DatabaseUsage usage = (DatabaseUsage) it2.next ();
            DatabaseContext context = (DatabaseContext) it2.next ();
            DatabaseDefinition definition = rootContext.getDefinition (usage.getName (), usage.getOffset ());
            if (definition != null) {
                definition.addUsage (usage);
                context.addUsage (usage);
                usage.setDatabaseDefinition (definition);
            }
        }
        return rootContext;
    }
    
    private static void process (
        List<ASTItem>           path, 
        DatabaseContext         context, 
        List<DatabaseItem>      unresolvedUsages,
        Document                doc,
        ParserManager           parser
    ) {
        ASTItem last = path.get (path.size () - 1);
        Iterator<ASTItem> it = last.getChildren ().iterator ();
        while (it.hasNext ()) {
            if (parser != null && parser.getState () == State.PARSING)
                return;
            ASTItem item =  it.next ();
            path.add (item);
            Language language = (Language) item.getLanguage ();
            if (language != null) {
                ASTPath astPath = ASTPath.create (path);
                Feature feature = language.getFeatureList ().getFeature ("SEMANTIC_DECLARATION", astPath);
                if (feature != null) {
                    SyntaxContext sc = SyntaxContext.create (doc, astPath);
                    String name = ((String) feature.getValue ("name", sc)).trim ();
                    String type = (String) feature.getValue ("type", sc);
                    if (name != null && name.length() > 0) {
                        //S ystem.out.println("add " + name + " " + item);
                        String local = (String) feature.getValue ("local", sc);
                        if (local != null) {
                            DatabaseContext c = context;
                            while (c != null && !local.equals (c.getType ()))
                                c = c.getParent ();
                            if (c != null) 
                                type = "local";
                        }
                        DatabaseContext con = context;
                        if ("method".equals(type)) { // NOI18N
                            con = con.getParent();
                            if (con == null) {
                                con = context;
                            }
                        }
                        con.addDefinition (new DatabaseDefinition (name, type, item.getOffset (), item.getEndOffset ()));
                    }
                }
                feature = language.getFeatureList ().getFeature ("SEMANTIC_CONTEXT", astPath);
                if (feature != null) {
                    String type = (String) feature.getValue ("type");
                    DatabaseContext newContext = new DatabaseContext (context, type, item.getOffset (), item.getEndOffset ());
                    context.addContext (item, newContext);
                    process (path, newContext, unresolvedUsages, doc, parser);
                    path.remove (path.size () - 1);
                    continue;
                }
                feature = language.getFeatureList ().getFeature ("SEMANTIC_USAGE", astPath);
                if (feature != null) {
                    SyntaxContext sc = SyntaxContext.create (doc, astPath);
                    String name = (String) feature.getValue ("name", sc);
                    DatabaseDefinition definition = context.getDefinition (name, item.getOffset ());
                    DatabaseUsage usage = new DatabaseUsage (name, item.getOffset (), item.getEndOffset ());
                    if (definition != null) {
                        definition.addUsage (usage);
                        usage.setDatabaseDefinition (definition);
                        context.addUsage (usage);
                    } else {
                        unresolvedUsages.add (usage);
                        unresolvedUsages.add (context);
                    }
                }
            }
            process (path, context, unresolvedUsages, doc, parser);
            path.remove (path.size () - 1);
        }
    }
    
    //private static Map<ASTNode,DatabaseContext> astNodeToDatabaseContext = new WeakHashMap<ASTNode,DatabaseContext> ();
    
    public static DatabaseContext getRoot (ASTNode ast) {
        return org.netbeans.api.languages.database.DatabaseManager.getRoot(ast);
        //return astNodeToDatabaseContext.get (ast);
    }
    
    static void setRoot (ASTNode node, DatabaseContext databaseContext) {
        org.netbeans.api.languages.database.DatabaseManager.setRoot(node, databaseContext);
        //astNodeToDatabaseContext.put (node, databaseContext);
    }
}

