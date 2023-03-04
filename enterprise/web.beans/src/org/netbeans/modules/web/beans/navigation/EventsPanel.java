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
package org.netbeans.modules.web.beans.navigation;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.swing.JLabel;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class EventsPanel extends BindingsPanel {

    private static final long serialVersionUID = -965978443984786734L;

    public EventsPanel( Object[] subject, 
            MetadataModel<WebBeansModel> metaModel , WebBeansModel model , 
            EventsModel uiModel )
    {
        super(subject, metaModel, model, uiModel);
        initLabels();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.InjectablesPanel#getSubjectElement(org.netbeans.api.java.source.ElementHandle, org.netbeans.modules.web.beans.api.model.WebBeansModel)
     */
    @Override
    protected Element getSubjectElement( Element context, WebBeansModel model )
    {
        ExecutableElement method = (ExecutableElement)context;
        return model.getObserverParameter( method );
    }

    private void initLabels() {
        JLabel typeLabel = getSubjectElementLabel();
        Mnemonics.setLocalizedText(typeLabel,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_ObservedEventType") );       // NOI18N
        typeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_ObservedEventType"));       // NOI18N
        typeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_ObservedEventType"));       // NOI18N
        
        JLabel qualifiersLabel= getSubjectBindingsLabel();
        Mnemonics.setLocalizedText(qualifiersLabel,NbBundle.getMessage( 
                ObserversPanel.class, "LBL_ObservedEventQualifiers") );  // NOI18N
        qualifiersLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSN_ObservedEventQualifiers"));  // NOI18N
        qualifiersLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage( 
                ObserversPanel.class, "ACSD_ObservedEventQualifiers"));  // NOI18N
    }
}
