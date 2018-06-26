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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.beans.CdiUtil;
import org.netbeans.modules.web.beans.api.model.BeansModel;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.navigation.InjectablesModel;
import org.netbeans.modules.web.beans.navigation.InjectablesPopup;
import org.netbeans.modules.web.beans.navigation.PopupUtil;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import com.sun.source.util.TreePath;


/**
 * @author ads
 *
 */
public class GoToDecoratorAtCaretAction extends AbstractCdiAction {
    
    private static final long serialVersionUID = 6777839777383350958L;

    private static final String GOTO_DECORATOR_AT_CARET =
        "go-to-decorator-at-caret";                     // NOI18N
    
    private static final String GOTO_DECORATOR_AT_CARET_POPUP =
        "go-to-decorator-at-caret-popup";               // NOI18N
    
    public GoToDecoratorAtCaretAction( ) {
        super(NbBundle.getMessage(GoToInjectableAtCaretAction.class, 
                GOTO_DECORATOR_AT_CARET));
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractCdiAction#findContext(javax.swing.text.JTextComponent, java.lang.Object[])
     */
    @Override
    protected boolean findContext( final JTextComponent component, 
            final Object[] context )
    {
        JavaSource javaSource = JavaSource.forDocument(component.getDocument());
        if ( javaSource == null ){
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        try {
            javaSource.runUserActionTask(  new Task<CompilationController>(){
                @Override
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase( Phase.ELEMENTS_RESOLVED );
                    int dot = component.getCaret().getDot();
                    TreePath tp = controller.getTreeUtilities().pathFor(dot);
                    Element contextElement = controller.getTrees().getElement(tp );
                    if ( contextElement == null ){
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(
                                        WebBeansActionHelper.class, 
                                "LBL_ElementNotFound"));
                        return;
                    }
                    context[0] = contextElement;
                }
            }, true );
            }
        catch (IOException e) {
            Logger.getLogger(GoToDecoratorAtCaretAction.class.getName()).log(
                    Level.INFO, e.getMessage(), e);
        }
        boolean result = context[0] instanceof TypeElement;
        
        if ( !result ){
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(GoToDecoratorAtCaretAction.class, 
                            "LBL_NotTypeElement"),                     // NOI18N
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        }
        else {
            context[0] = ElementHandle.create( (TypeElement) context[0]);
        }
            
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractWebBeansAction#modelAcessAction(org.netbeans.modules.web.beans.api.model.WebBeansModel, org.netbeans.modules.j2ee.metadata.model.api.MetadataModel, java.lang.Object[], javax.swing.text.JTextComponent, org.openide.filesystems.FileObject)
     */
    @Override
    protected void modelAcessAction( WebBeansModel model,
            final MetadataModel<WebBeansModel> metaModel, Object[] subject,
            final JTextComponent component, FileObject fileObject )
    {
        Element element = ((ElementHandle<?>)subject[0]).resolve( 
                model.getCompilationController());
        if (element == null) {
            return;
        }
        Collection<TypeElement> decorators = model.getDecorators((TypeElement)element);
        if (decorators.size() == 0) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(GoToDecoratorAtCaretAction.class,
                            "LBL_DecoratorsNotFound"),              // NOI18N
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
            return;
        }
        
        BeansModel beansModel = model.getModelImplementation().getBeansModel();
        final LinkedHashSet<TypeElement> enabledDecorators = WebBeansActionHelper.
                getEnabledDecorators(decorators, beansModel, null, 
                        model.getCompilationController());
        if (enabledDecorators.size() == 0) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(GoToDecoratorAtCaretAction.class,
                            "LBL_EnabledDecoratorsNotFound"),        // NOI18N
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
            return;
        }
        
        if (enabledDecorators.size() == 1) {
            final ElementHandle<TypeElement> handle = ElementHandle
                    .create(enabledDecorators.iterator().next());
            final ClasspathInfo classpathInfo = model
                    .getCompilationController().getClasspathInfo();
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    ElementOpen.open(classpathInfo, handle);
                }
            });
        }
        else  {
            final CompilationController controller = model
                    .getCompilationController();
            if (SwingUtilities.isEventDispatchThread()) {
                showPopup(enabledDecorators, controller, metaModel, component);
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        showPopup(enabledDecorators, controller, metaModel, component);
                    }
                });
            }
        }
    }

    private void showPopup( LinkedHashSet<TypeElement> elements , CompilationController 
            controller, MetadataModel<WebBeansModel> model ,JTextComponent target ) 
    {
        List<ElementHandle<? extends Element>> handles  = new ArrayList<
            ElementHandle<? extends Element>>(elements.size()); 
        for (TypeElement element : elements) {
            handles.add( ElementHandle.create( element ));
        }
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                InjectablesModel.class, "LBL_WaitNode"));
        try {
            Rectangle rectangle = target.modelToView(target.getCaret().getDot());
            Point point = new Point(rectangle.x, rectangle.y + rectangle.height);
            SwingUtilities.convertPointToScreen(point, target);

            String title = NbBundle.getMessage(
                    GoToInjectableAtCaretAction.class, "LBL_ChooseDecorator");
            PopupUtil.showPopup(new InjectablesPopup(title, handles, controller), title,
                    point.x, point.y);

        }
        catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractWebBeansAction#getActionCommand()
     */
    @Override
    protected String getActionCommand() {
        return GOTO_DECORATOR_AT_CARET;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractWebBeansAction#getPopupMenuKey()
     */
    @Override
    protected String getPopupMenuKey() {
        return GOTO_DECORATOR_AT_CARET_POPUP;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractCdiAction#handleProject(org.netbeans.api.project.Project, java.awt.event.ActionEvent)
     */
    @Override
    protected void handleProject( Project project , ActionEvent event ) {
        String msg = null;
        if ( event == null ){
            msg = "USG_CDI_GO_TO_DECORATOR_GLYPH";      // NOI18N
        }
        else {
            msg = "USG_CDI_GO_TO_DECORATOR";            // NOI18N
        }
        CdiUtil logger = project.getLookup().lookup(CdiUtil.class);
        if (logger != null) {
            logger.log(msg, 
                    GoToDecoratorAtCaretAction.class, new Object[] { project
                            .getClass().getName() });
        }
    }

}
