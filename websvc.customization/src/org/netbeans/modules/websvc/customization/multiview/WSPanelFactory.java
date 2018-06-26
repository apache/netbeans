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
 *
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

/*
 * WSPanelFactory.java
 *
 * Created on February 27, 2006, 11:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.websvc.customization.multiview.WSCustomizationView.BindingKey;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.openide.nodes.Node;

/**
 *
 * @author Roderico Cruz
 */
public class WSPanelFactory implements InnerPanelFactory {
    private ToolBarDesignEditor editor;
    private Node node;
    
    private Map<Object, SaveableSectionInnerPanel> panels;
    
    //panels
    private DefinitionsPanel definitionsPanel;
    private PortTypePanel portTypePanel;
    private PortTypeOperationPanel portTypeOperationPanel;
    private PortTypeOperationFaultPanel portTypeOperationFaultPanel;
    private BindingPanel bindingPanel;
    private BindingOperationPanel bindingOperationPanel;
    private ServicePanel servicePanel;
    private PortPanel portPanel;
    private ExternalBindingPanel externalBindingPanel;
    private Definitions primaryDefinitions;
    /**
     * Creates a new instance of WSPanelFactory
     */
    public WSPanelFactory(ToolBarDesignEditor editor,
            Node node, Definitions primaryDefinitions) {
        this.editor = editor;
        this.node = node;
        this.primaryDefinitions = primaryDefinitions;
        
        panels = new HashMap<Object, SaveableSectionInnerPanel>();
    }
    
    public Collection<SaveableSectionInnerPanel> getPanels(){
        return panels.values();
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        if(key instanceof Definitions){
            Definitions definitions = (Definitions)key;
            definitionsPanel = (DefinitionsPanel)panels.get(definitions);
            if(definitionsPanel == null){
                definitionsPanel =  new DefinitionsPanel((SectionView) editor.getContentView(),
                        definitions, node);
                panels.put(definitions, definitionsPanel);
            }
            return definitionsPanel;
        } else if (key instanceof PortType){
            PortType portType = (PortType)key;
            portTypePanel = (PortTypePanel)panels.get(portType);
            if(portTypePanel == null){
                portTypePanel = new PortTypePanel((SectionView) editor.getContentView(),
                        portType, node, primaryDefinitions);
                panels.put(portType, portTypePanel);
            }
            return portTypePanel;
        } else if (key instanceof Operation){
            Operation operation = (Operation)key;
            portTypeOperationPanel = (PortTypeOperationPanel)panels.get(operation);
            if(portTypeOperationPanel == null){
                portTypeOperationPanel = new PortTypeOperationPanel((SectionView) editor.getContentView(),
                        operation,  node, primaryDefinitions);
                panels.put(operation, portTypeOperationPanel);
            }
            return portTypeOperationPanel;
        } else if (key instanceof Fault){
            Fault fault = (Fault)key;
            portTypeOperationFaultPanel = (PortTypeOperationFaultPanel)panels.get(fault);
            if(portTypeOperationFaultPanel == null){
                portTypeOperationFaultPanel =  new PortTypeOperationFaultPanel((SectionView) editor.getContentView(),
                        fault);
                panels.put(fault, portTypeOperationFaultPanel);
            }
            return portTypeOperationFaultPanel;
        } else if (key instanceof Binding){
            Binding binding = (Binding)key;
            bindingPanel = (BindingPanel)panels.get(binding);
            if(bindingPanel == null){
                bindingPanel =  new BindingPanel((SectionView) editor.getContentView(),
                        binding, primaryDefinitions);
                panels.put(binding, bindingPanel);
            }
            return bindingPanel;
            
        } else if (key instanceof BindingOperation){
            BindingOperation bindingOperation = (BindingOperation)key;
            bindingOperationPanel  = (BindingOperationPanel)panels.get(bindingOperation);
            if(bindingOperationPanel == null){
                bindingOperationPanel =  new BindingOperationPanel((SectionView) editor.getContentView(),
                        bindingOperation, primaryDefinitions);
                panels.put(bindingOperation, bindingOperationPanel);
            }
            return bindingOperationPanel;
        } else if (key instanceof Service){
            Service service = (Service)key;
            servicePanel = (ServicePanel)panels.get(service);
            if(servicePanel == null){
                servicePanel =  new ServicePanel((SectionView) editor.getContentView(),
                        service);
                panels.put(service, servicePanel);
            }
            return servicePanel;
        } else if (key instanceof Port){
            Port port = (Port)key;
            portPanel = (PortPanel)panels.get(port);
            if(portPanel == null){
                portPanel =  new PortPanel((SectionView) editor.getContentView(),
                        port, node);
                panels.put(port, portPanel);
            }
            return portPanel;
        } else if (node.getLookup().lookup(JaxWsService.class) == null && 
                   key instanceof BindingKey ) {
            BindingKey bindingKey = (BindingKey)key;
            externalBindingPanel = (ExternalBindingPanel)panels.get(bindingKey);
            if(externalBindingPanel == null){
                externalBindingPanel =  new ExternalBindingPanel((SectionView) editor.getContentView(), node);
                panels.put(bindingKey,externalBindingPanel);
            }
            return externalBindingPanel;
        }
        return null;
    }
}
