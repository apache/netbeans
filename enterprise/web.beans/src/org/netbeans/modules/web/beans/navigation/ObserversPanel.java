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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
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
public class ObserversPanel extends BindingsPanel {

    private static final long serialVersionUID = -5038408349629504998L;

    public ObserversPanel( Object[] subject, 
            MetadataModel<WebBeansModel> metaModel , WebBeansModel model , 
            ObserversModel uiModel )
    {
        super(subject, metaModel, model , uiModel );
        initLabels();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.InjectablesPanel#setInjectableType(javax.lang.model.type.TypeMirror, org.netbeans.api.java.source.CompilationController)
     */
    @Override
    protected void setContextElement( Element context,
            CompilationController controller )
    {
        TypeMirror typeMirror = context.asType();
        TypeMirror parameterType = ((DeclaredType)typeMirror).getTypeArguments().get( 0 );
        super.setContextType(parameterType, controller);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.InjectablesPanel#getQualifiedElement(javax.lang.model.element.Element, org.netbeans.modules.web.beans.api.model.WebBeansModel)
     */
    @Override
    protected Element getSelectedQualifiedElement( Element context , 
            WebBeansModel model) 
    {
        if ( context.getKind() == ElementKind.METHOD){
            return model.getObserverParameter((ExecutableElement)context );
        }
        return context;
    }

    private void initLabels() {
        JLabel typeLabel = getSubjectElementLabel();
        Mnemonics.setLocalizedText(typeLabel,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_EventType") );       // NOI18N
        typeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_EventType"));       // NOI18N
        typeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_EventType"));       // NOI18N
        
        JLabel qualifiersLabel= getSubjectBindingsLabel();
        Mnemonics.setLocalizedText(qualifiersLabel,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_EventQualifiers") );       // NOI18N
        qualifiersLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_EventQualifiers"));       // NOI18N
        qualifiersLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_EventQualifiers"));       // NOI18N
    }
    
}
