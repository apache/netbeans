/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

