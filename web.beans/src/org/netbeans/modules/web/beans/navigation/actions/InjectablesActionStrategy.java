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

import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.VariableElement;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.Result;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.navigation.InjectablesModel;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public final class InjectablesActionStrategy implements ModelActionStrategy {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy#isApplicable(org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy.InspectActionId)
     */
    @Override
    public boolean isApplicable( InspectActionId id ) {
        return id == InspectActionId.INJECTABLES_CONTEXT ;
    }

    @Override
    public boolean isApplicable( WebBeansModel model, Object context[] ) {
        final VariableElement var = WebBeansActionHelper.findVariable(model, context);
        if (var == null) {
            return false;
        }
        try {
            if ( model.isEventInjectionPoint(var)){
                return false;
            }
            if (!model.isInjectionPoint(var)) {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(GoToInjectableAtCaretAction.class,
                                "LBL_NotInjectionPoint"), // NOI18N
                        StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                return false;
            }
        }
        catch (InjectionPointDefinitionError e) {
            StatusDisplayer.getDefault().setStatusText(e.getMessage(),
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy#invokeModelAction(org.netbeans.modules.web.beans.api.model.WebBeansModel, org.netbeans.modules.j2ee.metadata.model.api.MetadataModel, java.lang.Object[], javax.swing.text.JTextComponent, org.openide.filesystems.FileObject)
     */
    @Override
    public void invokeModelAction( final WebBeansModel model,
            final MetadataModel<WebBeansModel> metaModel, final Object[] subject,
            JTextComponent component, FileObject fileObject )
    {
        final VariableElement var = WebBeansActionHelper.findVariable(model, 
                subject);
        DependencyInjectionResult result = var== null? null: model.lookupInjectables(var, null, new AtomicBoolean(false));
        if (result == null) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(GoToInjectableAtCaretAction.class,
                            "LBL_InjectableNotFound"), // NOI18N
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
        CompilationController controller = model
                .getCompilationController();
        final InjectablesModel uiModel = new InjectablesModel(result, controller, 
                metaModel );
        final String name = var.getSimpleName().toString();
        final Result res = (result instanceof Result) ? (Result)result :null;
        if (SwingUtilities.isEventDispatchThread()) {
            WebBeansActionHelper.showInjectablesDialog(metaModel, model, 
                    subject , uiModel , name , res );
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                
                @Override
                public void run() {
                    WebBeansActionHelper.showInjectablesDialog(metaModel, 
                            null , subject ,uiModel , name , res);
                }
            });
        }
    }

}
