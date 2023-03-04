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

import java.util.Iterator;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.api.languages.Context;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.modules.languages.parser.SyntaxError;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.Line.Part;
import org.openide.text.NbDocument;


/**
 *
 * @author Jan Jancura
 */
public class ToolTipAnnotation extends Annotation {

    static final String TOOLTIP = "TOOLTIP";
    
    public String getShortDescription () {
        try {
            Part lp = (Part) getAttachedAnnotatable();
            if (lp == null) return null;
            Line line = lp.getLine ();
            DataObject dob = DataEditorSupport.findDataObject (line);
            EditorCookie ec = dob.getCookie (EditorCookie.class);
            NbEditorDocument document = (NbEditorDocument) ec.getDocument ();
            String mimeType = (String) document.getProperty ("mimeType");
            int offset = NbDocument.findLineOffset ( 
                    ec.getDocument (),
                    lp.getLine ().getLineNumber ()
                ) + lp.getColumn ();
            TokenHierarchy tokenHierarchy = TokenHierarchy.get (document);
            if (tokenHierarchy == null) return null;
            Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
            document.readLock ();
            try {
                TokenSequence tokenSequence = tokenHierarchy.tokenSequence ();
                if (tokenSequence == null)
                    return null;
                tokenSequence.move (offset);
                if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) return null;
                Token token = tokenSequence.token ();
                Feature tooltip = l.getFeatureList ().getFeature (TOOLTIP, token.id ().name ());
                if (tooltip != null) {
                    String s = c ((String) tooltip.getValue (Context.create (document, offset)));
                    return s;
                }
            } finally {
                document.readUnlock ();
            }
            ASTNode ast = null;
            ParserManagerImpl parserManager = ParserManagerImpl.getImpl (document);
            if (parserManager == null) return null;
            ast = parserManager.getAST ();
            if (ast == null) return null;
            ASTPath path = ast.findPath (offset);
            if (path == null) return null;
            int i, k = path.size ();
            for (i = 0; i < k; i++) {
                ASTPath p = path.subPath (i);
                Feature tooltip = l.getFeatureList ().getFeature (TOOLTIP, p);
                if (tooltip == null) continue;
                String s = c ((String) tooltip.getValue (SyntaxContext.create (document, p)));
                return s;
            }
            Iterator<SyntaxError> it = parserManager.getSyntaxErrors ().iterator ();
            while (it.hasNext ()) {
                SyntaxError syntaxError = it.next ();
                ASTItem item = syntaxError.getItem ();
                if (item.getOffset () == ast.getEndOffset ())
                    item = ast.findPath (item.getOffset () - 1).getLeaf ();
                if (item.getOffset () > offset) break;
                if (item.getEndOffset () > offset) {
                    return syntaxError.getMessage ();
                }
            }
        } catch (LanguageDefinitionNotFoundException ex) {
        }
        return null;
    }

    public String getAnnotationType () {
        return null; // Currently return null annotation type
    }
    
    private static String c (String s) {
        if (s == null) return null;
        s = s.replace ("\\n", "\n");
        s = s.replace ("\\r", "\r");
        s = s.replace ("\\t", "\t");
        return s;
    }
}

