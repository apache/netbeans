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

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.jakarta.web.beans.api.model.InterceptorsResult;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.jakarta.web.beans.navigation.InterceptorsModel;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public final class InterceptorsActionStrategy implements ModelActionStrategy {

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.navigation.actions.ModelActionStrategy#isApplicable(org.netbeans.modules.jakarta.web.beans.navigation.actions.ModelActionStrategy.InspectActionId)
     */
    @Override
    public boolean isApplicable( InspectActionId id ) {
        return id == InspectActionId.CLASS_CONTEXT|| id == InspectActionId.METHOD_CONTEXT;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.navigation.actions.ModelActionStrategy#isApplicable(org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel, java.lang.Object[])
     */
    @Override
    public boolean isApplicable( WebBeansModel model, Object[] context ) {
        final Object handle = context[0];
        if ( handle == null ){
            return false;
        }
        Element element = ((ElementHandle<?>)handle).resolve( 
                model.getCompilationController());
        if (context[2] == InspectActionId.METHOD_CONTEXT) {
            if ( !( element instanceof ExecutableElement)) {
                return false;
            }
            return model.getInterceptorBindings(element).size() >0 ;
        }
        // Now check all interceptor bindings for element
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.navigation.actions.ModelActionStrategy#invokeModelAction(org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel, org.netbeans.modules.j2ee.metadata.model.api.MetadataModel, java.lang.Object[], javax.swing.text.JTextComponent, org.openide.filesystems.FileObject)
     */
    @Override
    public void invokeModelAction( WebBeansModel model,
            final MetadataModel<WebBeansModel> metaModel, final Object[] subject,
            JTextComponent component, FileObject fileObject )
    {
        final Object handle = subject[0];
        Element element = ((ElementHandle<?>)handle).resolve( 
                model.getCompilationController());
        if ( element == null ){
            return;
        }
        CompilationController controller = model.getCompilationController();
        final InterceptorsResult result = model.getInterceptors(element);
        final InterceptorsModel uiModel = new InterceptorsModel( 
               result , controller, metaModel);
        final String name = element.getSimpleName().toString();
        if (SwingUtilities.isEventDispatchThread()) {
            WebBeansActionHelper.showInterceptorsDialog( metaModel, model, 
                    subject , uiModel , name, result );
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    WebBeansActionHelper.showInterceptorsDialog(metaModel, null , 
                            subject ,uiModel , name , result);
                }
            });
        }

    }

}
