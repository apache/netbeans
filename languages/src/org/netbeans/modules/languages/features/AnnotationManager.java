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

import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.openide.ErrorManager;
import org.openide.text.Annotation;


/**
 *
 * @author Jan Jancura
 */
public class AnnotationManager extends ASTEvaluator {
    
    private NbEditorDocument            doc;
    private ParserManager               parser;
    private List<ASTItem>               items;
    private List<Feature>               marks;
    private List<LanguagesAnnotation>   annotations = new ArrayList<LanguagesAnnotation> ();

    
    /** Creates a new instance of AnnotationManager */
    public AnnotationManager (Document doc) {
        this.doc = (NbEditorDocument) doc;
        if (doc == null) throw new NullPointerException ();
        parser = ParserManager.get (doc);
        parser.addASTEvaluator (this);
    }
    
    public String getFeatureName () {
        return "MARK";
    }

    public void beforeEvaluation (State state, ASTNode root) {
        items = new ArrayList<ASTItem> ();
        marks = new ArrayList<Feature> ();
    }

    public void afterEvaluation (State state, ASTNode root) {
        refresh (items, marks);
    }

    public void evaluate (State state, List<ASTItem> path, Feature feature) {
        if (feature.getBoolean ("condition", SyntaxContext.create (doc, ASTPath.create (path)), true)) {
            items.add (path.get (path.size () - 1));
            marks.add (feature);
        }
    }
    
    public void remove () {
        removeAnnotations ();
        parser.removeASTEvaluator (this);
    }
    
    private void removeAnnotations () {
        Iterator<LanguagesAnnotation> it = annotations.iterator ();
        while (it.hasNext ())
            doc.removeAnnotation (it.next ());
    }
    
    private void refresh (final List<ASTItem> items, final List<Feature> marks) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                try {
                    List<LanguagesAnnotation> newAnnotations = new ArrayList<LanguagesAnnotation> ();
                    Iterator<LanguagesAnnotation> it = annotations.iterator ();
                    LanguagesAnnotation oldAnnotation = it.hasNext () ? it.next () : null;
                    Iterator<ASTItem> it2 = items.iterator ();
                    Iterator<Feature> it3 = marks.iterator ();
                    int count = 0;
                    while (it2.hasNext () && count < 100) {
                        ASTItem item = it2.next ();
                        Feature mark = it3.next ();
                        String message = (String) mark.getValue ("message");
                        Language language = (Language) item.getLanguage ();
                        message = LocalizationSupport.localize (language, message);
                        String type = (String) mark.getValue ("type");
                        while (
                            oldAnnotation != null &&
                            oldAnnotation.getPosition ().getOffset () < item.getOffset ()
                        ) {
                            doc.removeAnnotation (oldAnnotation);
                            oldAnnotation = it.hasNext () ? it.next () : null;
                        }
                        count++;
                        if (
                            oldAnnotation != null &&
                            oldAnnotation.getPosition ().getOffset () == item.getOffset () &&
                            oldAnnotation.getAnnotationType ().equals (type) &&
                            oldAnnotation.getShortDescription ().equals (message)
                        ) {
                            newAnnotations.add (oldAnnotation);
                            oldAnnotation = it.hasNext () ? it.next () : null;
                            continue;
                        }
                        LanguagesAnnotation la = new LanguagesAnnotation (
                            type,
                            message
                        );
   
                        if (item.getLength() == 0) {
                            //when the ASTItem length is zero we need to find an appropriate token to signal the error 
                            TokenHierarchy hi = TokenHierarchy.get(doc);
                            TokenSequence ts = hi.tokenSequence();
                            ts.move(item.getOffset());
                            //test if next token contains the ASTItem's language embedding
                            if(!(ts.moveNext() && testCreateAnnotation(hi, ts, item, la)))
                                //if not, do the same with previous token
                                if(!(ts.movePrevious() && testCreateAnnotation(hi, ts, item, la))) {
                                    //give up - use default annotation location
                                    Position position = doc.createPosition(item.getOffset ());
                                    la.setPosition (position);
                                    doc.addAnnotation(doc.createPosition(item.getOffset()), item.getLength(), la);
                                }
                        } else {
                            Position position = doc.createPosition(item.getOffset ());
                            la.setPosition (position);
                            doc.addAnnotation(doc.createPosition(item.getOffset()), item.getLength(), la);
                        }
                        newAnnotations.add (la);
                    } // while
                    if (oldAnnotation != null)
                        doc.removeAnnotation (oldAnnotation);
                    while (it.hasNext ())
                        doc.removeAnnotation (it.next ());
                    annotations = newAnnotations;
                } catch (BadLocationException ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            }
        });
    }

    private boolean testCreateAnnotation(TokenHierarchy hi, TokenSequence ts, ASTItem item, LanguagesAnnotation la) throws BadLocationException {
        if (ts.language () == null)
            throw new NullPointerException ("ts.language()==null");
        if (ts.language ().mimeType () == null)
            throw new NullPointerException ("TokenSequence.mimeType==null");
        if (ts.language().mimeType().equals(item.getMimeType())) {
                Token t = ts.token();
                if (t == null) throw new NullPointerException ();
                Position position = doc.createPosition(t.offset(hi));
                la.setPosition (position);
                doc.addAnnotation(position, t.length(), la);
                return true;
            } else {
                ts = ts.embedded();
                if(ts == null) {
                    return false;
                } else {
                    return ts.moveNext() ? testCreateAnnotation(hi, ts, item, la) : false;
                }
            }
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
            if (type == null) throw new NullPointerException ();
            if (description == null) throw new NullPointerException ();
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

