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

import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.VariableElement;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.jakarta.web.beans.api.model.InjectionPointDefinitionError;
import org.netbeans.modules.jakarta.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.jakarta.web.beans.api.model.Result;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.jakarta.web.beans.navigation.InjectablesModel;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public final class InjectablesActionStrategy implements ModelActionStrategy {

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.navigation.actions.ModelActionStrategy#isApplicable(org.netbeans.modules.jakarta.web.beans.navigation.actions.ModelActionStrategy.InspectActionId)
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
     * @see org.netbeans.modules.jakarta.web.beans.navigation.actions.ModelActionStrategy#invokeModelAction(org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel, org.netbeans.modules.j2ee.metadata.model.api.MetadataModel, java.lang.Object[], javax.swing.text.JTextComponent, org.openide.filesystems.FileObject)
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
