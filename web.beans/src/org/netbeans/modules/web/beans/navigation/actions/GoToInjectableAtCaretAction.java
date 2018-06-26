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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import org.netbeans.modules.web.beans.CdiUtil;
import org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.navigation.InjectablesModel;
import org.netbeans.modules.web.beans.navigation.InjectablesPopup;
import org.netbeans.modules.web.beans.navigation.PopupUtil;
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
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractWebBeansAction#getActionCommand()
     */
    @Override
    protected String getActionCommand() {
        return GOTO_INJACTABLE_AT_CARET;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractWebBeansAction#getPopupMenuKey()
     */
    @Override
    protected String getPopupMenuKey() {
        return GOTO_INJACTABLE_AT_CARET_POPUP;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractWebBeansAction#modelAcessAction(org.netbeans.modules.web.beans.api.model.WebBeansModel, org.netbeans.modules.j2ee.metadata.model.api.MetadataModel, java.lang.Object[], javax.swing.text.JTextComponent, org.openide.filesystems.FileObject)
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
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractCdiAction#handleProject(org.netbeans.api.project.Project, java.awt.event.ActionEvent)
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
