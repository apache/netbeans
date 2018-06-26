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
package org.netbeans.modules.web.beans.navigation.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ImplementationProvider;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.beans.hints.CDIAnnotation;
import org.netbeans.modules.web.beans.hints.CDIAnnotation.CDIAnnotaitonType;
import org.netbeans.modules.web.beans.hints.EditorAnnotationsHelper;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.NbBundle;



/**
 * @author ads
 *
 */
public class CdiGlyphAction extends AbstractAction {

    public CdiGlyphAction(){
        putValue(NAME, NbBundle.getMessage(CdiGlyphAction.class, "TXT_GlyphActionName"));  // NOI18N
        putValue("supported-annotation-types", new String[] {
            "org-netbeans-modules-web-beans-annotations-injection-point",
            "org-netbeans-modules-web-beans-annotations-delegate-point",
            "org-netbeans-modules-web-beans-annotations-decorated-bean",
            "org-netbeans-modules-web-beans-annotations-event",
            "org-netbeans-modules-web-beans-annotations-observer",
            "org-netbeans-modules-editor-annotations-intercepted"
        });
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed( ActionEvent event ) {
        JTextComponent comp = (JTextComponent) event.getSource();
        if (!performCdiAction(comp)) {
            Action actions[] = ImplementationProvider.getDefault().
                getGlyphGutterActions(comp);

            if (actions == null)
                return ;

            int nextIndex = 0;

            while (nextIndex < actions.length && actions[nextIndex] != this) {
                nextIndex++;
            }

            nextIndex++;

            if (actions.length > nextIndex) {
                Action action = actions[nextIndex];
                if (action!=null && action.isEnabled()){
                    action.actionPerformed(event);
                }
            }
        }
    }

    private boolean performCdiAction( final JTextComponent comp ) {
        final Document document = comp.getDocument();
        if ( document instanceof BaseDocument ){
            final Object values[] = new Object[2];
            document.render( new Runnable() {
                
                @Override
                public void run() {
                    int dot = comp.getCaret().getDot();
                    Annotations annotations = ((BaseDocument) document).getAnnotations();
                    Line line = NbEditorUtilities.getLine(document, dot, false);
                    if (line == null) {
                        return ;
                    }
                    int lineNumber = line.getLineNumber();
                    AnnotationDesc  desc = annotations.getActiveAnnotation(lineNumber);
                    values[0] = lineNumber;
                    values[1] = desc;
                }
            });
            
            if ( values[0] == null || values[1] ==  null ){
                return false;
            }
            int lineNumber = (Integer) values[0];
            AnnotationDesc desc = (AnnotationDesc)values[1];
            String annotationType = desc.getAnnotationType();
            EditorAnnotationsHelper helper = EditorAnnotationsHelper.
                getInstance( getFile(comp));
            if ( helper == null ){
                return false;
            }
            List<CDIAnnotation> cdiAnnotations = helper.getAnnotations();
            for (CDIAnnotation cdiAnnotation : cdiAnnotations) {
                String cdiAnnotationType = cdiAnnotation.getAnnotationType();
                if ( cdiAnnotationType.equals( annotationType)){
                    Line cdiLine = cdiAnnotation.getPart().getLine();
                    if ( cdiLine.getLineNumber() == lineNumber ){
                        int length = cdiAnnotation.getPart().getLength();
                        if ( length == desc.getLength() ){
                            doAction( comp , annotationType, cdiAnnotation.getPart(),
                                    (BaseDocument)document);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    

    private void doAction( JTextComponent comp ,  String annotationType , 
            final Line.Part part , BaseDocument doc  ) 
    {
        if ( annotationType.equals( CDIAnnotation.CDIAnnotaitonType.
                INJECTION_POINT.toString()) || annotationType.equals( 
                        CDIAnnotation.CDIAnnotaitonType.DELEGATE_POINT.toString()))
        {
            final AnnotationPositionStrategy strategy = 
                new AnnotationPositionStrategy(part, doc);
            GoToInjectableAtCaretAction action = new GoToInjectableAtCaretAction(){
                
                /* (non-Javadoc)
                 * @see org.netbeans.modules.web.beans.navigation.actions.AbstractInjectableAction#findContext(javax.swing.text.JTextComponent, java.lang.Object[])
                 */
                @Override
                protected boolean findContext( JTextComponent component,
                        Object[] context )
                {
                    return WebBeansActionHelper.getVariableElementAtDot( component, 
                            context , true , strategy );
                }
            };
            action.actionPerformed(null, comp);
            
        }
        else if ( annotationType.equals( CDIAnnotaitonType.EVENT.toString() )){
            final AnnotationPositionStrategy strategy = 
                new AnnotationPositionStrategy(part, doc);
            InspectCDIAtCaretAction action = new InspectCDIAtCaretAction(){
                /* (non-Javadoc)
                 * @see org.netbeans.modules.web.beans.navigation.actions.InspectCDIAtCaretAction#findContext(javax.swing.text.JTextComponent, java.lang.Object[])
                 */
                @Override
                protected boolean findContext( JTextComponent component,
                        Object[] subject )
                {
                    return WebBeansActionHelper.getVariableElementAtDot( component, 
                            subject , false , strategy) || WebBeansActionHelper.
                            getContextEventInjectionAtDot( component, subject , strategy );
                }
            };
                
            action.actionPerformed(null, comp);
        }
        else if ( annotationType.equals( CDIAnnotaitonType.OBSERVER.toString() )){
            final AnnotationPositionStrategy strategy = 
                new AnnotationPositionStrategy(part, doc);
            InspectCDIAtCaretAction action = new InspectCDIAtCaretAction(){
                /* (non-Javadoc)
                 * @see org.netbeans.modules.web.beans.navigation.actions.InspectCDIAtCaretAction#findContext(javax.swing.text.JTextComponent, java.lang.Object[])
                 */
                @Override
                protected boolean findContext( JTextComponent component,
                        Object[] subject )
                {
                    return WebBeansActionHelper.getMethodAtDot(component, 
                            subject, strategy );
                }
            };
                
            action.actionPerformed(null, comp);
        }
        else if ( annotationType.equals( CDIAnnotaitonType.INTERCEPTED_ELEMENT.toString() )){
            final AnnotationPositionStrategy strategy = 
                new AnnotationPositionStrategy(part, doc);
            InspectCDIAtCaretAction action = new InspectCDIAtCaretAction(){
                /* (non-Javadoc)
                 * @see org.netbeans.modules.web.beans.navigation.actions.InspectCDIAtCaretAction#findContext(javax.swing.text.JTextComponent, java.lang.Object[])
                 */
                @Override
                protected boolean findContext( JTextComponent component,
                        Object[] subject )
                {
                    return WebBeansActionHelper.getMethodAtDot(component, 
                            subject, strategy ) || WebBeansActionHelper.
                            getClassAtDot(component, subject, strategy);
                }
            };
                
            action.actionPerformed(null, comp);
        }
        else if ( annotationType.equals( CDIAnnotaitonType.DECORATED_BEAN.toString() )){
            final AnnotationPositionStrategy strategy = 
                new AnnotationPositionStrategy(part, doc);
            GoToDecoratorAtCaretAction action = new GoToDecoratorAtCaretAction(){
                /* (non-Javadoc)
                 * @see org.netbeans.modules.web.beans.navigation.actions.InspectCDIAtCaretAction#findContext(javax.swing.text.JTextComponent, java.lang.Object[])
                 */
                @Override
                protected boolean findContext( JTextComponent component,
                        Object[] subject )
                {
                    return WebBeansActionHelper.
                            getClassAtDot(component, subject, strategy);
                }
            };
                
            action.actionPerformed(null, comp);
        }
    }

    private FileObject getFile(JTextComponent component) {
        Document doc = component.getDocument();
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);

        if (od == null) {
            return null;
        }

        return od.getPrimaryFile();
    }

    private static class AnnotationPositionStrategy implements 
        PositionStrategy, Runnable 
    {
        
        AnnotationPositionStrategy(Line.Part part, BaseDocument doc ){
            myPart = part;
            myDocument = doc;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            Line line = myPart.getLine();
            int startOffset = Utilities.getRowStartFromLineOffset(myDocument, 
                    line.getLineNumber());
            myOffset = startOffset+myPart.getColumn();
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.beans.navigation.actions.PositionStrategy#getOffset(javax.swing.text.JTextComponent)
         */
        @Override
        public int getOffset( JTextComponent component ) {
            Document document = component.getDocument();
            document.render( this );
            return myOffset;
        }
       
        private Line.Part myPart;
        private BaseDocument myDocument;
        private int myOffset;
    }
}
