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
import java.util.Iterator;
import java.util.List;
import javax.swing.text.Position;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.modules.languages.parser.SyntaxError;
import org.openide.ErrorManager;
import org.openide.text.Annotation;
import org.openide.text.NbDocument;


/**
 *
 * @author Jan Jancura
 */
public class SyntaxErrorHighlighter implements ParserManagerListener {
    
    private NbEditorDocument            doc;
    private ParserManagerImpl           parserManager;
    private List<LanguagesAnnotation>   annotations = new ArrayList<LanguagesAnnotation> ();

    
    /** Creates a new instance of SyntaxErrorHighlighter */
    public SyntaxErrorHighlighter (Document doc) {
        
        this.doc = (NbEditorDocument) doc;
        parserManager = ParserManagerImpl.getImpl (doc);
        parserManager.addListener (this);
    }

    public void parsed (State state, final ASTNode root) {
        final List<SyntaxError> newErrors = new ArrayList<SyntaxError> (parserManager.getSyntaxErrors ());
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                try {
                    List<LanguagesAnnotation> newAnnotations = new ArrayList<LanguagesAnnotation> ();
                    Iterator<LanguagesAnnotation> oldIterator = annotations.iterator ();
                    LanguagesAnnotation oldAnnotation = oldIterator.hasNext () ? oldIterator.next () : null;
                    Iterator<SyntaxError> newIterator = newErrors.iterator ();
                    int lastLineNumber = -1;
                    int count = 0;
                    while (newIterator.hasNext () && count < 100) {
                        SyntaxError syntaxError = newIterator.next ();
                        ASTItem item = syntaxError.getItem ();
                        String message = syntaxError.getMessage ();
                        while (
                            oldAnnotation != null &&
                            oldAnnotation.getPosition ().getOffset () < item.getOffset ()
                        ) {
                            doc.removeAnnotation (oldAnnotation);
                            oldAnnotation = oldIterator.hasNext () ? oldIterator.next () : null;
                        }
                        count++;
                        if (
                            oldAnnotation != null &&
                            oldAnnotation.getPosition ().getOffset () == item.getOffset () &&
                            oldAnnotation.getShortDescription ().equals (message)
                        ) {
                            int ln = NbDocument.findLineNumber (doc, oldAnnotation.getPosition ().getOffset ());
                            if (ln > lastLineNumber)
                                newAnnotations.add (oldAnnotation);
                            else
                                doc.removeAnnotation (oldAnnotation);
                            lastLineNumber = ln;
                            oldAnnotation = oldIterator.hasNext () ? oldIterator.next () : null;
                            continue;
                        }
                        int ln = NbDocument.findLineNumber (doc, item.getOffset ());
                        if (ln == lastLineNumber) continue;
                        
                        LanguagesAnnotation la = new LanguagesAnnotation (
                            "SyntaxError",
                            message
                        );
                        Position position = doc.createPosition (item.getOffset ());
                        la.setPosition (position);
                        doc.addAnnotation (position, item.getLength (), la);
                        newAnnotations.add (la);
                        lastLineNumber = ln;
                    } // while
                    if (oldAnnotation != null)
                        doc.removeAnnotation (oldAnnotation);
                    while (oldIterator.hasNext ())
                        doc.removeAnnotation (oldIterator.next ());
                    annotations = newAnnotations;
                } catch (BadLocationException ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            }
        });
    }
    
    
    // innerclasses ............................................................
    
    static class LanguagesAnnotation extends Annotation {

        private String type;
        private String description;

        /** Creates a new instance of ToolsAnotation */
        LanguagesAnnotation (
            String type,
            String description
        ) {
            this.type = type;
            this.description = description;
        }

        /** Returns name of the file which describes the annotation type.
         * The file must be defined in module installation layer in the
         * directory "Editors/AnnotationTypes"
         * @return  name of the anotation type
         */
        public String getAnnotationType () {
            return type;
        }

        /** Returns the tooltip text for this annotation.
         * @return  tooltip for this annotation
         */
        public String getShortDescription () {
            return description;
        }
        
        private Position position;
        
        void setPosition (Position position) {
            this.position = position;
        }
        
        Position getPosition () {
            return position;
        }
    }
}

