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

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 * @author  Rico Cruz
 */
public class WSCustomizationTopComponent extends TopComponent {

    static final long serialVersionUID=6021472310161712674L;
    private boolean initialized = false;
    private WSPanelFactory panelFactory = null;
    private Set<WSDLModel> models;
    private Node node;
    private boolean isLight;
    private Definitions primaryDefinitions;
    
    public WSCustomizationTopComponent(){
    }
    
    public WSCustomizationTopComponent(Node node, Set<WSDLModel> models, 
            Definitions primaryDefinitions, boolean isLight) {
        setLayout(new BorderLayout());        
        initialized = false;
        this.node = node;
        this.models = models;
        this.isLight = isLight;
        this.primaryDefinitions = primaryDefinitions;
    }
    
    @Override
    protected String preferredID(){
        return "CustomizationComponent";    //NOI18N
    }
    
  
    public Collection<SaveableSectionInnerPanel> getPanels(){
        if ( panelFactory == null ){
            return Collections.emptyList();
        }
        else {
            return panelFactory.getPanels();
        }
    }
    
    private void doInitialize() {
        initAccessibility();

        if (primaryDefinitions != null) {
            ToolBarDesignEditor tb = new ToolBarDesignEditor();
            panelFactory = new WSPanelFactory(tb, node, primaryDefinitions);
            WSCustomizationView mview = new WSCustomizationView(panelFactory,
                    models, primaryDefinitions, isLight);
            tb.setContentView(mview);
            add(tb);
        }
        setFocusable(true);
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    private void initAccessibility(){

    }
     
    @Override
    public void addNotify() {
        if (!initialized) {
            initialized = true;
            doInitialize();
        }
        super.addNotify();
    }
    

    @Override
    protected void componentShowing() {
        if (!initialized) {
            initialized = true;
            doInitialize();
        }
        super.componentShowing();
    }
    
}

