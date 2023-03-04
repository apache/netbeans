/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.beans.navigation.actions;

import java.util.Collection;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.beans.api.model.BeansModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.navigation.DecoratorsModel;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public final class DecoratoresActionStrategy implements ModelActionStrategy {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy#isApplicable(org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy.InspectActionId)
     */
    @Override
    public boolean isApplicable( InspectActionId id ) {
        return id == InspectActionId.CLASS_CONTEXT;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy#isApplicable(org.netbeans.modules.web.beans.api.model.WebBeansModel, java.lang.Object[])
     */
    @Override
    public boolean isApplicable( WebBeansModel model, Object[] context ) {
        final Object handle = context[0];
        if ( handle == null ){
            return false;
        }
        Element element = ((ElementHandle<?>)handle).resolve( 
                model.getCompilationController());
        if ( element == null ){
            return false;
        }
        List<AnnotationMirror> qualifiers = model.getQualifiers(element,  true);
        // if class has qualifiers then Class context is considered as decorator context
        if ( qualifiers.size() >0 ){
            return true;
        }
        /*
         *  If it doesn't have explicit qualifiers then it could have implicit @Default
         *  qualifier . In the latter case check Interceptor Bindings presence 
         */
        return model.getInterceptorBindings(element).isEmpty();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy#invokeModelAction(org.netbeans.modules.web.beans.api.model.WebBeansModel, org.netbeans.modules.j2ee.metadata.model.api.MetadataModel, java.lang.Object[], javax.swing.text.JTextComponent, org.openide.filesystems.FileObject)
     */
    @Override
    public void invokeModelAction( WebBeansModel model,
            final MetadataModel<WebBeansModel> metaModel, final Object[] subject,
            JTextComponent component, FileObject fileObject )
    {
        final Object handle = subject[0];
        Element element = ((ElementHandle<?>)handle).resolve( 
                model.getCompilationController());
        if ( !( element instanceof TypeElement) ){
            return;
        }
        TypeElement type = (TypeElement)element;
        CompilationController controller = model.getCompilationController();
        Collection<TypeElement> decorators = model.getDecorators(type);
        BeansModel beansModel = model.getModelImplementation().getBeansModel();
        final DecoratorsModel uiModel = new DecoratorsModel(decorators, 
                beansModel, controller, metaModel);
        final String name = type.getSimpleName().toString();
        if (SwingUtilities.isEventDispatchThread()) {
            WebBeansActionHelper.showDecoratorsDialog( metaModel, model, 
                    subject , uiModel , name );
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    WebBeansActionHelper.showDecoratorsDialog(metaModel, null , 
                            subject ,uiModel , name );
                }
            });
        }
    }
}
