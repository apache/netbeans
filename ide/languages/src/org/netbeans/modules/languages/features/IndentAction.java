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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit.InsertBreakAction;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Feature.Type;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;


/**
 *
 * @author Jan Jancura
 */
public class IndentAction extends InsertBreakAction {

    protected void afterBreak (
        JTextComponent target, 
        BaseDocument doc, 
        Caret caret, 
        Object cookie
    ) {
        try {
            TokenHierarchy th = TokenHierarchy.get (doc);
            TokenSequence ts = th.tokenSequence ();
            ts.move (caret.getDot ());
            if (ts.moveNext ())
                while (ts.embedded () != null) {
                    ts = ts.embedded ();
                    ts.move (caret.getDot ());
                    if (!ts.moveNext ()) break;
                }
            Language l = LanguagesManager.getDefault ().getLanguage (ts.language ().mimeType ());
            Token token = ts.token ();
            Object indentValue = getIndentProperties (l);

            if (indentValue == null) return;
            if (indentValue instanceof Object[]) {
                Object[] params = (Object[]) indentValue;
                int length = doc.getLength();
                int ln = NbDocument.findLineNumber ((StyledDocument) doc, caret.getDot () - 1);
                int endLine = NbDocument.findLineNumber ((StyledDocument) doc, length - 1);
                int start = NbDocument.findLineOffset ((StyledDocument) doc, ln);
                int end = ln < endLine ? NbDocument.findLineOffset ((StyledDocument) doc, ln + 1) : length;
                String line = doc.getText (start, end - start);
                int indent = getIndent (line);
                ts.move (start);
                ts.moveNext ();
                int ni = getIndent (line, ts, end, params);
                if (ni > 0)
                    indent += 4;
                else
                if (ni == 0 && ln > 0) {
                    int start1 = NbDocument.findLineOffset ((StyledDocument) doc, ln - 1);
                    line = doc.getText (start1, start - start1);
                    ts.move (start1);
                    ts.moveNext ();
                    ni = getIndent (line, ts, start, params);
                    if (ni == 2)
                        indent -= 4;
                }
                try {
                    start = NbDocument.findLineOffset ((StyledDocument) doc, ln + 1);
                    try {
                        end = NbDocument.findLineOffset ((StyledDocument) doc, ln + 2);
                        line = doc.getText (start, end - start);
                    } catch (IndexOutOfBoundsException ex) {
                        line = doc.getText (start, doc.getLength () - start);
                    }
                } catch (IndexOutOfBoundsException ex) {
                    line = null;
                }
                indent (doc, caret.getDot (), indent);
                if ( line != null && 
                     ((Set) params [2]).contains (line.trim ())
                ) {
                    indent -= 4;
                    int offset = caret.getDot ();
                    doc.insertString (offset, "\n", null);
                    indent (doc, caret.getDot (), indent);
                    caret.setDot (offset);
                }
            } else
            if (indentValue instanceof Feature) {
                Feature m = (Feature) indentValue;
                m.getValue (Context.create (doc, ts.offset ()));
            }
            
        } catch (LanguageDefinitionNotFoundException ldnfe) {
            //no language found - this might happen when some of the embedded languages are not schliemann based,
            //so just ignore and do nothing - no indent
        } catch (Exception ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }

    private static int getIndent (String line) {
        int indent = 0;
        int i = 0, k = line.length () - 1;
        while (i < k && Character.isWhitespace (line.charAt (i))) {
            if (line.charAt(i) == '\t') {
                indent += 8 - indent % 8;
            } else {
                indent++;
            }
            i++;
        }
        return indent;
    }

    private static int getIndent (
        String line, 
        TokenSequence ts, 
        int end, 
        Object[] params
    ) {
        Map p = new HashMap ();
        do {
            Token t = ts.token ();
            String id = t.text ().toString ();
            if (((Set) params [1]).contains (id)) {
                Integer i = (Integer) p.get (id);
                if (i == null) {
                    i = Integer.valueOf (1);
                } else
                    i = Integer.valueOf (i.intValue () + 1);
                p.put (id, i);
            }
            if (((Set) params [2]).contains (t.text ().toString ())) {
                id = (String) ((Map) params [3]).get (id);
                Integer i = (Integer) p.get (id);
                if (i == null) {
                    i = Integer.valueOf (-1);
                } else
                    i = Integer.valueOf (i.intValue () - 1);
                p.put (id, i);
            }
            if (!ts.moveNext ()) break;
        } while (ts.offset () < end);
        Iterator it = p.values ().iterator ();
        while (it.hasNext ()) {
            int i = ((Integer) it.next ()).intValue ();
            if (i > 0) return 1;
            if (i < 0) return -1;
        }
        it = ((List) params [0]).iterator ();
        while (it.hasNext ()) {
            Pattern pattern = (Pattern) it.next ();
            if (pattern.matcher (line).matches ())
                return 2;
        }
        return 0;
    }

    private static void indent (Document doc, int offset, int i) {
        StringBuilder sb = new StringBuilder ();
        while (i > 0) {
            sb.append (' ');i--;
        }
        try {
            doc.insertString (offset, sb.toString (), null);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
    private static Map indentProperties = new WeakHashMap ();
    
    private static Object getIndentProperties (Language l) {
        if (!indentProperties.containsKey (l)) {
            List<Pattern> patterns = new ArrayList<Pattern> ();
            Set<String> start = new HashSet<String> ();
            Set<String> end = new HashSet<String> ();
            Map<String,String> endToStart = new HashMap<String,String> ();
            
            List<Feature> indents = l.getFeatureList ().getFeatures ("INDENT");
            Iterator<Feature> it = indents.iterator ();
            while (it.hasNext ()) {
                Feature indent = it.next ();
                if (indent.getType () == Type.METHOD_CALL) {
                    return indent;
                }
                String s = (String) indent.getValue ();
                int i = s.indexOf (':');
                if (i < 1) {
                    patterns.add (Pattern.compile (c (s)));
                    continue;
                }
                start.add (s.substring (0, i));
                end.add (s.substring (i + 1));
                endToStart.put (s.substring (i + 1), s.substring (0, i));
            }
            indentProperties.put (
                l,
                new Object[] {patterns, start, end, endToStart}
            );
        }
        return indentProperties.get (l);
    }
    
    private static String c (String s) {
        s = s.replace ("\\n", "\n");
        s = s.replace ("\\r", "\r");
        s = s.replace ("\\t", "\t");
        s = s.replace ("\\\"", "\"");
        s = s.replace ("\\\'", "\'");
        s = s.replace ("\\\\", "\\");
        return s;
    }
}
