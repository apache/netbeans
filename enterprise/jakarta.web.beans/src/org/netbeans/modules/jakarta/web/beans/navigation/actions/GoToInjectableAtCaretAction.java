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
package org.netbeans.modules.jakarta.web.beans.navigation.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.jakarta.web.beans.CdiUtil;
import org.netbeans.modules.jakarta.web.beans.api.model.InjectionPointDefinitionError;
import org.netbeans.modules.jakarta.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.jakarta.web.beans.navigation.InjectablesModel;
import org.netbeans.modules.jakarta.web.beans.navigation.InjectablesPopup;
import org.netbeans.modules.jakarta.web.beans.navigation.PopupUtil;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class GoToInjectableAtCaretAction extends AbstractInjectableAction {

    private static final long serialVersionUID = -6998124281864635094L;

    private static final String GOTO_INJACTABLE_AT_CARET =
        "go-to-injactable-at-caret";                     // NOI18N
    
    private static final String GOTO_INJACTABLE_AT_CARET_POPUP =
        "go-to-injactable-at-caret-popup";               // NOI18N

    public GoToInjectableAtCaretAction() {
        super(NbBundle.getMessage(GoToInjectableAtCaretAction.class, 
                GOTO_INJACTABLE_AT_CARET));
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.navigation.actions.AbstractWebBeansAction#getActionCommand()
     */
    @Override
    protected String getActionCommand() {
        return GOTO_INJACTABLE_AT_CARET;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.navigation.actions.AbstractWebBeansAction#getPopupMenuKey()
     */
    @Override
    protected String getPopupMenuKey() {
        return GOTO_INJACTABLE_AT_CARET_POPUP;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.navigation.actions.AbstractWebBeansAction#modelAcessAction(org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel, org.netbeans.modules.j2ee.metadata.model.api.MetadataModel, java.lang.Object[], javax.swing.text.JTextComponent, org.openide.filesystems.FileObject)
     */
    /**
     * Variable element is resolved based on containing type element 
     * qualified name and simple name of variable itself.
     * Model methods are used further for injectable resolution.   
     */
    @Override
    protected void modelAcessAction( WebBeansModel model,
            final MetadataModel<WebBeansModel> metaModel, Object[] variable,
            final JTextComponent component, FileObject fileObject )
    {
        VariableElement var = WebBeansActionHelper.findVariable(model, variable);
        if (var == null) {
            return;
        }
        try {
            if ( !model.isInjectionPoint(var) ){
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(GoToInjectableAtCaretAction.class, 
                                "LBL_NotInjectionPoint"),            // NOI18N
                        StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                return;
            }
        }
        catch (InjectionPointDefinitionError e) {
            StatusDisplayer.getDefault().setStatusText(e.getMessage(),
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        }
        final DependencyInjectionResult result = model.lookupInjectables(var, null, new AtomicBoolean(false));
        if (result == null) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(GoToInjectableAtCaretAction.class,
                            "LBL_InjectableNotFound"),              // NOI18N
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
            return;
        }
        if (result instanceof DependencyInjectionResult.Error) {
            StatusDisplayer.getDefault().setStatusText(
                    ((DependencyInjectionResult.Error) result).getMessage(),
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        }
        if (result.getKind() == DependencyInjectionResult.ResultKind.DEFINITION_ERROR) {
            return;
        }
        if (result.getKind() == DependencyInjectionResult.ResultKind.INJECTABLE_RESOLVED) {
            Element injectable = ((DependencyInjectionResult.InjectableResult) result)
                    .getElement();
            if(injectable != null) {//may be null for ee specific implemantations, which may not be present on classpath and may not have sources
                final ElementHandle<Element> handle = ElementHandle
                        .create(injectable);
                final ClasspathInfo classpathInfo = model
                        .getCompilationController().getClasspathInfo();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        ElementOpen.open(classpathInfo, handle);
                    }
                });
            }
        }
        else if (result.getKind() == DependencyInjectionResult.ResultKind.RESOLUTION_ERROR) {
            final CompilationController controller = model
                    .getCompilationController();
            if (SwingUtilities.isEventDispatchThread()) {
                showPopup(result, controller, metaModel, component);
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        showPopup(result, controller, metaModel, component);
                    }
                });
            }
        }
    }

    private void showPopup( DependencyInjectionResult result , CompilationController controller, 
            MetadataModel<WebBeansModel> model ,JTextComponent target ) 
    {
        if ( !(result instanceof DependencyInjectionResult.ApplicableResult)){
            return;
        }
        Set<TypeElement> typeElements = ((DependencyInjectionResult.ApplicableResult)result).getTypeElements();
        Set<Element> productions = ((DependencyInjectionResult.ApplicableResult)result).getProductions();
        if ( typeElements.size() +productions.size() == 0 ){
            return;
        }
        List<ElementHandle<? extends Element>> handles  = new ArrayList<
            ElementHandle<? extends Element>>(typeElements.size() +productions.size()); 
        for (Element element : typeElements) {
            if ( !((DependencyInjectionResult.ApplicableResult)result).isDisabled(element)){
                handles.add( ElementHandle.create( element ));
            }
        }
        for (Element element : productions) {
            if ( !((DependencyInjectionResult.ApplicableResult)result).isDisabled(element)){
                handles.add( ElementHandle.create( element ));
            }
        }
        if ( handles.size() == 0 ){
            return;
        }
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                InjectablesModel.class, "LBL_WaitNode"));       // NOI18N
        try {
            Rectangle rectangle = target.modelToView(target.getCaret().getDot());
            Point point = new Point(rectangle.x, rectangle.y + rectangle.height);
            SwingUtilities.convertPointToScreen(point, target);

            String title = NbBundle.getMessage(
                    GoToInjectableAtCaretAction.class, "LBL_ChooseInjectable");
            PopupUtil.showPopup(new InjectablesPopup(title, handles, controller), title,
                    point.x, point.y);

        }
        catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.navigation.actions.AbstractCdiAction#handleProject(org.netbeans.api.project.Project, java.awt.event.ActionEvent)
     */
    @Override
    protected void handleProject( Project project , ActionEvent event ) {
        String msg = null;
        if ( event == null ){
            msg = "USG_CDI_GO_TO_INJECTABLE_GLYPH";           // NOI18N
        }
        else {
            msg = "USG_CDI_GO_TO_INJECTABLE";                 // NOI18N
        }
        CdiUtil logger = project.getLookup().lookup(CdiUtil.class);
        if (logger != null) {
            logger.log(msg, 
                    GoToInjectableAtCaretAction.class, new Object[] { project
                            .getClass().getName() });
        }
    }

}
