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
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Feature.Type;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.Utils;
import org.netbeans.modules.editor.indent.spi.Context.Region;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;


/**
 *
 * @author Jan Jancura
 */
public class IndentFactory implements IndentTask.Factory {

    
    public IndentTask createTask (org.netbeans.modules.editor.indent.spi.Context context) {
        return new GLFIndentTask (context);
    }

    private static class GLFIndentTask implements IndentTask {
        
        private org.netbeans.modules.editor.indent.spi.Context context;
        
        private GLFIndentTask (org.netbeans.modules.editor.indent.spi.Context context) {
            this.context = context;
        }

        public void reindent () throws BadLocationException {
            //S ystem.out.println("SCHLIEMAN reformat !\n  " + context.document() + "\n  " + context.isIndent() + "\n  " + context.startOffset () + "\n  " + context.endOffset());
            StyledDocument document = (StyledDocument) context.document ();
            try {
                MimePath mimePath = MimePath.parse (context.mimePath ());
                String mimeType = mimePath.getMimeType (mimePath.size () - 1);
                Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
                Object indentValue = getIndentProperties (l);
                if (indentValue == null) return;
                
                if (indentValue instanceof Feature) {
                    Feature m = (Feature) indentValue;
                    m.getValue (Context.create (document, context.startOffset ()));
                    return;
                }
                Object[] params = (Object[]) indentValue;
                
                TokenHierarchy tokenHierarchy = TokenHierarchy.get (document);
                LanguagePath languagePath = LanguagePath.get (org.netbeans.api.lexer.Language.find (mimePath.getMimeType (0)));
                for (int i = 1; i < mimePath.size(); i++)
                    languagePath = languagePath.embedded (org.netbeans.api.lexer.Language.find (mimePath.getMimeType (i)));
                List<TokenSequence> tokenSequences = tokenHierarchy.tokenSequenceList (languagePath, context.startOffset (), context.endOffset ());
                
                Set<Integer> whitespaces = l.getAnalyser().getSkipTokenTypes ();
                
                Iterator<Region> it = context.indentRegions ().iterator ();
                while (it.hasNext ()) {
                    Region region = it.next ();
                    Map<Position,Integer> indentMap = new HashMap<Position,Integer> ();
                    int ln = NbDocument.findLineNumber (document, region.getStartOffset ());
                    int endLineNumber = NbDocument.findLineNumber (document, region.getEndOffset ());
                    if (!Utils.getTokenSequence (document, context.lineStartOffset (region.getStartOffset ())).language ().mimeType ().equals (mimeType)) 
                        ln++;
                    int indent = 0;
                    if (ln > 0) {
                        int offset = NbDocument.findLineOffset (document, ln - 1);
                        indent = context.lineIndent (offset);
                        if (!Utils.getTokenSequence (document, offset).language ().mimeType ().equals (mimeType))
                            indent += IndentUtils.indentLevelSize (document); 
                    }
                    while (ln <= endLineNumber) {
                        if (ln == endLineNumber && 
                            isEmpty (ln, document, whitespaces) &&
                            !Utils.getTokenSequence (document, region.getEndOffset ()).language ().mimeType ().equals (mimeType)
                        ) break;
                        indent = indent (context, document, params, ln++, indent, indentMap, whitespaces);
                    }

                    Iterator<Position> it2 = indentMap.keySet ().iterator ();
                    while (it2.hasNext ()) {
                        Position position = it2.next ();
                        context.modifyIndent (position.getOffset (), indentMap.get (position));
                    }
                }
            } catch (LanguageDefinitionNotFoundException ldnfe) {
                //no language found - this might happen when some of the embedded languages are not schliemann based,
                //so just ignore and do nothing - no indent
            } catch (Exception ex) {
                ErrorManager.getDefault ().notify (ex);
            }
        }

        public ExtraLock indentLock () {
            return null;
        }

        private int indent (
            org.netbeans.modules.editor.indent.spi.Context context,
            StyledDocument      document, 
            Object[]            params, 
            int                 ln, 
            int                 indent,
            Map<Position,Integer> indentMap,
            Set<Integer>         whitespaces
        ) throws BadLocationException {
            int ni = ln > 0 ? computeIndent (ln - 1, document, context, params) : 0;
            if (ni > 0)
                indent += IndentUtils.indentLevelSize (document); 
            else
            if (ni < 0) {
                if (!startsWithBrace (ln - 1, document, context, params, whitespaces))
                    indent -= IndentUtils.indentLevelSize (document); 
            } else
            if (ni == 0 && ln > 1) {
                ni = computeIndent (ln - 2, document, context, params);
                if (ni == 2)
                    indent -= IndentUtils.indentLevelSize (document);
            }
            if (startsWithBrace (ln, document, context, params, whitespaces))
                indent -= IndentUtils.indentLevelSize (document);
            indent = Math.max (indent, 0);
            indentMap.put (document.createPosition (NbDocument.findLineOffset (document, ln)), indent);
            //context.modifyIndent (end + 1, indent);
//            try {
//                start = NbDocument.findLineOffset (doc, ln + 1);
//                try {
//                    end = NbDocument.findLineOffset (doc, ln + 2);
//                    previousLine = doc.getText (start, end - start);
//                } catch (IndexOutOfBoundsException ex) {
//                    previousLine = doc.getText (start, doc.getLength () - start);
//                }
//            } catch (IndexOutOfBoundsException ex) {
//                previousLine = null;
//            }
//            indent (doc, start, indent);
//            if ( previousLine != null && 
//                 ((Set) params [2]).contains (previousLine.trim ())
//            ) {
//                indent -= 4;
//                doc.insertString (context.startOffset (), "\n", null);
//                indent (doc, context.startOffset (), indent);
//            }
            return indent;
        }
        
        private static int getCurrentIndent (String line) {
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

        private static int computeIndent (
            int                     ln,
            StyledDocument          document,
            org.netbeans.modules.editor.indent.spi.Context context,
            Object[]                params
        ) throws BadLocationException {
            int start = NbDocument.findLineOffset (document, ln);
            int end = document.getLength ();
            try {
                end = NbDocument.findLineOffset (document, ln + 1) - 1;
            } catch (IndexOutOfBoundsException ex) {
            }
            TokenSequence ts = Utils.getTokenSequence (document, start);
            Map<String,Integer> p = new HashMap<String,Integer> ();
            do {
                Token t = ts.token ();
                String id = t.text ().toString ().trim();
                if (((Set) params [1]).contains (id)) {
                    Integer i = p.get (id);
                    if (i == null) {
                        i = Integer.valueOf (1);
                    } else
                        i = Integer.valueOf (i.intValue () + 1);
                    p.put (id, i);
                }
                if (((Set) params [2]).contains (t.text ().toString ().trim())) {
                    id = (String) ((Map) params [3]).get (id);
                    Integer i = p.get (id);
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
            String line = document.getText (start, end - start);
            it = ((List) params [0]).iterator ();
            while (it.hasNext ()) {
                Pattern pattern = (Pattern) it.next ();
                if (pattern.matcher (line).matches ())
                    return 2;
            }
            return 0;
        }

        private static boolean startsWithBrace (
            int                     ln,
            StyledDocument          document,
            org.netbeans.modules.editor.indent.spi.Context context,
            Object[]                params,
            Set<Integer>            whitespaces
        ) throws BadLocationException {
            int start = NbDocument.findLineOffset (document, ln);
            int end = document.getLength ();
            try {
                end = NbDocument.findLineOffset (document, ln + 1) - 1;
            } catch (IndexOutOfBoundsException ex) {
            }
            TokenSequence ts = Utils.getTokenSequence (document, start);
            if (ts.token () == null) return false;
            while (whitespaces.contains (ts.token ().id ().ordinal ())) {
                if (!ts.moveNext ()) return false;
                if (ts.offset () > end) return false;
            }
            Token t = ts.token ();
            String id = t.text ().toString ();
            String trimedId = id.trim();
            // If id has more than 2 leading newlines, should return false
            int nlIdx = id.indexOf("\n");
            if (nlIdx >= 0) {
                nlIdx = id.indexOf("\n", nlIdx + 1);
                if (nlIdx >= 0 && nlIdx < id.indexOf(trimedId)) {
                    return false;
                }
            }
            return ((Set) params [2]).contains (trimedId);
        }

        private static boolean isEmpty (
            int                     ln,
            StyledDocument          document,
            Set<Integer>            whitespaces
        ) throws BadLocationException {
            int start = NbDocument.findLineOffset (document, ln);
            int end = document.getLength ();
            try {
                end = NbDocument.findLineOffset (document, ln + 1) - 1;
            } catch (IndexOutOfBoundsException ex) {
            }
            TokenSequence ts = Utils.getTokenSequence (document, start);
            if (ts.token () == null) return true;
            while (whitespaces.contains (ts.token ().id ().ordinal ())) {
                if (!ts.moveNext ()) return true;
                if (ts.offset () > end) return true;
            }
            return false;
        }

        private static Object getIndentProperties (Language l) {
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
            return indents.isEmpty() ? null : new Object[] {patterns, start, end, endToStart};
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
}





