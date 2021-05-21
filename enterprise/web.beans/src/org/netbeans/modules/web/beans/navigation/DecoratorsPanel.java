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
package org.netbeans.modules.web.beans.navigation;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.JEditorPane;
import javax.swing.JLabel;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class DecoratorsPanel extends BindingsPanel {

    private static final long serialVersionUID = -5097678699872215262L;
    private static final String DELEGATE = "javax.decorator.Delegate";  // NOI18N

    public DecoratorsPanel( Object[] subject, MetadataModel<WebBeansModel> metaModel,
            WebBeansModel model, JavaHierarchyModel treeModel )
    {
        super(subject, metaModel, model, treeModel);
        // use Scope components for showing type of Delegate injectoin point
        setVisibleScope( true );
        initUI();
    }
    
    @Override
    protected void setScope( WebBeansModel model, Element element ){
        TypeElement clazz = (TypeElement)element;
        List<VariableElement> fields = ElementFilter.fieldsIn(
                model.getCompilationController().getElements().getAllMembers( clazz));
        VariableElement delegate = null;
        for (VariableElement field : fields) {
            if( hasDelegate(field,  model.getCompilationController())){
                delegate = field;
                break;
            }
        }
        TypeMirror delegateType = delegate.asType();
        StringBuilder shortName = new StringBuilder();
        StringBuilder fqnName = new StringBuilder();
        fillElementType(delegateType, shortName, fqnName, model.getCompilationController());
        JEditorPane scopeComponent = getScopeComponent();
        if ( showFqns() ){
            scopeComponent.setText( fqnName.toString() );
        }
        else {
            scopeComponent.setText( shortName.toString() );
        }
    }
    
    private boolean hasDelegate( Element element , CompilationController controller){
        List<? extends AnnotationMirror> allAnnotationMirrors = controller.
            getElements().getAllAnnotationMirrors( element);
        TypeElement delegate = controller.getElements().getTypeElement( DELEGATE);
        if( delegate == null ){
            return false;
        }
        for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
            Element annotation = controller.getTypes().asElement( 
                    annotationMirror.getAnnotationType());
            if ( annotation!= null && annotation.equals( delegate )){
                return true;
            }
        }
        return false;
    }

    private void initUI() {
        JLabel elementLabel = getSubjectElementLabel();
        Mnemonics.setLocalizedText(elementLabel,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_DecoratedElement") );       // NOI18N
        elementLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_DecoratedElement"));       // NOI18N
        elementLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_DecoratedElement"));       // NOI18N
        
        JLabel qualifiers= getSubjectBindingsLabel();
        Mnemonics.setLocalizedText(qualifiers,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_DecoratorQualifiers") );  // NOI18N
        qualifiers.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_DecoratorQualifiers"));  // NOI18N
        qualifiers.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_DecoratorQualifiers"));  // NOI18N
        
        JLabel selectedQualifiers = getSelectedBindingsLabel();
        Mnemonics.setLocalizedText(selectedQualifiers,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_SelectedDecoratorQualifiers") );  // NOI18N
        selectedQualifiers.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_SelectedDecoratorQualifiers"));  // NOI18N
        selectedQualifiers.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_SelectedDecoratorQualifiers"));  // NOI18N
        
        JLabel scopeLabel = getScopeLabel();
        Mnemonics.setLocalizedText(scopeLabel,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_SelectedDelegateType") );  // NOI18N
        scopeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_SelectedDelegateType"));  // NOI18N
        scopeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_SelectedDelegateType"));  // NOI18N
        
    }

}
