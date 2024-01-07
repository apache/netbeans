/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * ErrorAnnotation.java
 *
 * Created on November 9, 2004, 3:09 PM
 */

package org.netbeans.modules.web.core.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.web.core.api.ErrorInfo;
import org.netbeans.modules.web.core.spi.ErrorAnnotation;
import org.netbeans.modules.web.core.spi.ErrorAnnotationFactory;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Petr Pisl, mfukala@netbeans.org
 */
public class ErrorAnnotationImpl implements ErrorAnnotation {
    
    
    /** Jsp file, for which is the ErrorAnnotation */
    private FileObject jspFo;
    
    private List<LineSetAnnotation> annotations;
    
    /** Creates a new instance of ErrorAnnotation */
    public ErrorAnnotationImpl(FileObject jspFo) {
        this.jspFo = jspFo;
        annotations = new ArrayList<>();
    }
    
    /** Adds annotation for the errors. If the error is already annotated, does nothing. If there are 
     *  annotated errors, which are not in the input array, then these annotations are deleted.
     *
     *  
     */
    @Override
    public void annotate(ErrorInfo[] errors){
        List<LineSetAnnotation> added, removed, unchanged;
        Collection<LineSetAnnotation> newAnnotations;
        
        // obtain data object
        DataObject doJsp;
        try {
            doJsp = DataObject.find(jspFo);
        }
        catch (DataObjectNotFoundException e){
            return;
        }
        
        EditorCookie editor = (EditorCookie)doJsp.getCookie(EditorCookie.class);
        if (editor == null)
            return;
        StyledDocument document = editor.getDocument();
        if (document == null)
            return;
        
        // Fix issue #59568
        if(editor.getOpenedPanes()==null)
            return;
        
        // The approriate JText component
        JTextComponent component = editor.getOpenedPanes()[0];
        if (component != null){
            if (errors != null && errors.length > 0){
                // Place the first error in the status bar
                org.netbeans.editor.Utilities.setStatusBoldText(component , " " + errors[0].getDescription()); //NOI18N
            }
            else{
                // clear status bar
                org.netbeans.editor.Utilities.clearStatusText(component);
            }
        }
        
        // create annotations from errors
        newAnnotations = getAnnotations(errors, document);
        // which annotations are really new
        added=new ArrayList<>(newAnnotations);
        added.removeAll(annotations);
        // which annotations were here before
        unchanged=new ArrayList<>(annotations);
        unchanged.retainAll(newAnnotations);
        // which annotations are obsolete
        removed = annotations;
        removed.removeAll(newAnnotations);
        detachAnnotations(removed);

        // are there new annotations?
        if (!added.isEmpty()) {
            final List<LineSetAnnotation>  finalAdded = added;
            final DataObject doJsp2 = doJsp;
            Runnable docRenderer = () -> {
                LineCookie cookie = (LineCookie)doJsp2.getCookie(LineCookie.class);
                Line.Set lines = cookie.getLineSet();
                                
                for (LineSetAnnotation ann : finalAdded) {
                    ann.attachToLineSet(lines);
                }
            };

            document.render(docRenderer);
        }
        
        // remember current annotations
        annotations=unchanged;
        annotations.addAll(added);
    }
    
    /** Transforms ErrosInfo to Annotation
     */
    private Collection<LineSetAnnotation> getAnnotations(ErrorInfo[] errors, StyledDocument document) {
        BaseDocument doc = (BaseDocument) document;
        Map<Integer, LineSetAnnotation> map = new HashMap<>(errors.length);
        for (int i = 0; i < errors.length; i ++) {
            ErrorInfo err = errors[i];
            int line = err.getLine();
            int column = err.getColumn();

            if (line<0){
                // place error annotation on the 1st non-empty line
                try {
                    int firstNonWS = Utilities.getFirstNonWhiteFwd(doc, 0);
                    line = Utilities.getLineOffset(doc, firstNonWS) + 1;
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            if (column < 0){
                column = 0;
            }
            
            String message = err.getDescription();
            LineSetAnnotation ann;
            switch (err.getType()){
                case ErrorInfo.JSP_ERROR:
                    ann = new JspParserErrorAnnotation(line, column, message, (NbEditorDocument)document);
                    break;
                default:
                    ann = new JspParserErrorAnnotation(line, column, message, (NbEditorDocument)document);
                    break;
            }
           

            // This is trying to ensure that annotations on the same
            // line are "chained" (so we get a single annotation for
            // multiple errors on a line).
            // If we knew the errors were sorted by file & line number,
            // this would be easy (and we wouldn't need to do the hashmap
            // "sort"
            Integer lineInt = line;
            /*LineSetAnnotation prev = (LineSetAnnotation)map.get(lineInt);
            if (prev != null) {
                prev.chain(ann);
            } else if (map.size() < maxErrors) {*/
            map.put(lineInt, ann);
            //}
        }
        return map.values();
    }
    
    /** Removes obsolete annotations
     */
    
    private static void detachAnnotations(Collection anns) {
        Iterator i;

        for (i=anns.iterator();i.hasNext();) {
            Annotation ann=(Annotation)i.next();
            if (ann.getAttachedAnnotatable() != null) {
                ann.detach();
            }
        }
    }
    
    public abstract static class LineSetAnnotation extends Annotation {

        public abstract void attachToLineSet(Line.Set lines);
    }
    
    @ServiceProvider(service=ErrorAnnotationFactory.class)
    public static class Factory implements ErrorAnnotationFactory {

        @Override
        public ErrorAnnotation create(FileObject file) {
            return new ErrorAnnotationImpl(file);
        }

    }
   
}
