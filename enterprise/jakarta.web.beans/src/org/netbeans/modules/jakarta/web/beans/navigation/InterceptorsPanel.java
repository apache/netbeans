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
package org.netbeans.modules.jakarta.web.beans.navigation;

import java.util.Collection;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.JLabel;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.jakarta.web.beans.api.model.CdiException;
import org.netbeans.modules.jakarta.web.beans.api.model.Result;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class InterceptorsPanel extends BindingsPanel {

    private static final long serialVersionUID = -3849331046190789438L;

    public InterceptorsPanel( Object[] subject,
            MetadataModel<WebBeansModel> metaModel, WebBeansModel model,
            JavaHierarchyModel treeModel, Result result )
    {
        super(subject, metaModel, model, treeModel, result);
        initLabels();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.navigation.BindingsPanel#setContextElement(javax.lang.model.element.Element, org.netbeans.api.java.source.CompilationController)
     */
    @Override
    protected void setContextElement( Element context,
            CompilationController controller )
    {
        if ( context instanceof ExecutableElement ){
            ExecutableElement method = (ExecutableElement) context;
            getShortElementName().append( context.getSimpleName().toString());
            appendMethodParams(getShortElementName(), method);
            
            TypeElement enclosingType = controller.getElementUtilities().
                enclosingTypeElement( context );
            String typeFqn = enclosingType.getQualifiedName().toString();
            getFqnElementName().append( typeFqn );
            getFqnElementName().append('.');
            getFqnElementName().append( context.getSimpleName().toString() );
            appendMethodParams(getFqnElementName(), method);
        }
        else if ( context instanceof TypeElement ){
            TypeElement type = (TypeElement) context;
            getShortElementName().append( type.getSimpleName().toString() );
            getFqnElementName().append( type.getQualifiedName().toString() );
        }
    }
    
    @Override
    protected void initBindings( WebBeansModel model, Element element){
        Collection<AnnotationMirror> interceptorBindings = model.getInterceptorBindings(element);
        
        StringBuilder fqnBuilder = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        
        for (AnnotationMirror annotationMirror : interceptorBindings) {
            appendAnnotationMirror(annotationMirror, fqnBuilder,  true );
            appendAnnotationMirror(annotationMirror, builder,  false );
        }
        if ( fqnBuilder.length() >0 ){
            setFqnBindings( fqnBuilder.substring(0 , fqnBuilder.length() -2 ));
            setShortBindings( builder.substring(0 , builder.length() -2 ));
        }
        else {
            setFqnBindings("");
            setShortBindings("");
        }
        if ( showFqns() ) {
            getInitialBindingsComponent().setText( getFqnBindings() );
        }
        else {
            getInitialBindingsComponent().setText( getShortBindings() );
        }
    }
    
    @Override
    protected void doShowSelectedCDI(ElementHandle<?> elementHandle,
            WebBeansModel model ) throws CdiException
    {
        Element element = elementHandle.resolve(
                model.getCompilationController());
        if ( element == null ){
            getSelectedBindingsComponent().setText("");
        }
        else {
            element = getSelectedQualifiedElement( element, model);
            Collection<AnnotationMirror> interceptorBindings = 
                model.getInterceptorBindings(element);
            StringBuilder builder = new StringBuilder();
            
            for (AnnotationMirror annotationMirror : interceptorBindings) {
                appendAnnotationMirror(annotationMirror, builder,  showFqns() );
            }
            String bindingsString = "";
            if ( builder.length() >0 ){
                bindingsString = builder.substring(0 , 
                        builder.length() -2 );
            }
            getSelectedBindingsComponent().setText( bindingsString);
            setStereotypes(model, element);
        }
    }
    
    private void appendMethodParams(StringBuilder builder, ExecutableElement method){
        builder.append('(');
        List<? extends VariableElement> parameters = method.getParameters();
        int i=0;
        for (VariableElement variableElement : parameters) {
            String param = variableElement.getSimpleName().toString();
            builder.append( param );
            if ( i < parameters.size() -1 ){
                builder.append(", ");
            }
            i++;
        }
        builder.append(')');
    }
    
    private void initLabels() {
        JLabel elementLabel = getSubjectElementLabel();
        Mnemonics.setLocalizedText(elementLabel,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_InterceptedElement") );       // NOI18N
        elementLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_InterceptedElement"));       // NOI18N
        elementLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_InterceptedElement"));       // NOI18N
        
        JLabel iBindingsLabel= getSubjectBindingsLabel();
        Mnemonics.setLocalizedText(iBindingsLabel,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_InterceptorBindings") );  // NOI18N
        iBindingsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_InterceptorBindings"));  // NOI18N
        iBindingsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_InterceptorBindings"));  // NOI18N
        
        JLabel selectedIBindgings = getSelectedBindingsLabel();
        Mnemonics.setLocalizedText(selectedIBindgings,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_SelectedInterceptorBindings") );  // NOI18N
        selectedIBindgings.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_SelectedInterceptorBindings"));  // NOI18N
        selectedIBindgings.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_SelectedInterceptorBindings"));  // NOI18N
        
        JLabel selectedStereotypes = getStereotypesLabel();
        Mnemonics.setLocalizedText(selectedStereotypes,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_IStereotypes") );  // NOI18N
        selectedStereotypes.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_IStereotypes"));  // NOI18N
        selectedStereotypes.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_IStereotypes"));  // NOI18N
    }

}
