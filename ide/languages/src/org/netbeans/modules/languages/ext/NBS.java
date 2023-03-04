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

package org.netbeans.modules.languages.ext;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.CompletionItem;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.LibrarySupport;
import org.netbeans.api.languages.SyntaxContext;

/**
 *
 * @author Jan Jancura
 */
public class NBS {
    
    private static final String DOC = "org/netbeans/modules/languages/resources/NBS-Library.xml";
    
    public static List<CompletionItem> completion (Context context) {
        if (context instanceof SyntaxContext) {
            SyntaxContext sContext = (SyntaxContext) context;
            ASTPath path = sContext.getASTPath ();
            String c = "root";
            ListIterator<ASTItem> it = path.listIterator (path.size () - 1);
            boolean properties = false;
            while (it.hasPrevious ()) {
                ASTItem item =  it.previous ();
                if (item instanceof ASTToken) continue;
                ASTNode node = (ASTNode) item;
                if (node.getNT ().equals ("regularExpression"))
                    return Collections.<CompletionItem>emptyList ();
                else
                if (node.getNT ().equals ("selector"))
                    return Collections.<CompletionItem>emptyList ();
                else
                if (node.getNT ().equals ("value"))
                    properties = true;
                else
                if (node.getNT ().equals ("command")) {
                    String p = node.getTokenTypeIdentifier ("keyword");
                    if (p != null && properties) {
                        c = p;
                        break;
                    }
                }
            }
            return getLibrary ().getCompletionItems (c);
        }
        return Collections.<CompletionItem>emptyList ();
    }
    
    private static LibrarySupport library;
    
    private static LibrarySupport getLibrary () {
        if (library == null)
            library = LibrarySupport.create (DOC);
        return library;
    }

    public static boolean tokenComand (SyntaxContext context) {
        ASTPath path = context.getASTPath ();
        if (path.size () < 3) return false;
        ASTNode node = (ASTNode) path.get (path.size () - 3);
        ASTToken keywordToken = node.getTokenType ("keyword");
        return keywordToken != null && keywordToken.getIdentifier ().equals ("TOKEN");
    }

    public static boolean notTokenComand (SyntaxContext context) {
        ASTPath path = context.getASTPath ();
        Iterator<ASTItem> it = path.listIterator ();
        while (it.hasNext ()) {
            ASTItem item = it.next ();
            if (item instanceof ASTNode) {
                ASTNode node = (ASTNode) item;
                if (node.getNT ().equals ("command")) {
                    ASTToken keywordToken = node.getTokenType ("keyword");
                    return keywordToken != null && !keywordToken.getIdentifier ().equals ("TOKEN");
                }
            }
        }
        return false;
    }
}




