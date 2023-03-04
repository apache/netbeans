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
package org.netbeans.modules.websvc.wsitconf.design;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.javamodel.ServiceChangeListener;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Martin Grebac
 */
public class WsitServiceChangeListener implements ServiceChangeListener {
    
    Service service;
    Project project;
    DataObject dataObject;
    
    public WsitServiceChangeListener(Service service, DataObject dataObject, Project project) {
        this.service = service;
        this.project = project;
        this.dataObject = dataObject;
    }
    
    public void propertyChanged(String propertyName, String oldValue, String newValue) { }
    
    public void operationAdded(MethodModel method) {
        
        Binding binding = WSITModelSupport.getBinding(service, 
                dataObject.getPrimaryFile(), project, false, null);
        if (binding != null) {
            BindingOperation bO = getBindingOperation(binding, method.getOperationName());
            if(bO == null){
                bO = Util.generateOperation(binding, Util.getPortType(binding), method.getOperationName(), method.getImplementationClass());
            }
            if (SecurityPolicyModelHelper.isSecurityEnabled(binding)) {
                String profile = ProfilesModelHelper.getSecurityProfile(binding);
                ProfilesModelHelper.getInstance(PolicyModelHelper.getConfigVersion(bO)).setMessageLevelSecurityProfilePolicies(bO, profile);
            }
            WSITModelSupport.save(binding);
        }
    }
    
    private BindingOperation getBindingOperation(Binding binding, String operationName ){
        Collection<BindingOperation> bindingOperations = binding.getBindingOperations();
        for(BindingOperation bindingOperation : bindingOperations){
            if(bindingOperation.getName().equals(operationName)){
                return bindingOperation;
            }
        }
        return null;
    }
    
    public void operationRemoved(MethodModel method) {
        Binding binding = WSITModelSupport.getBinding(service, 
                dataObject.getPrimaryFile(), project, false, null);
        if (binding != null) {
            WSDLModel model = binding.getModel();
            String methodName = method.getOperationName();
            Definitions d = model.getDefinitions();
            Collection<Message> messages = d.getMessages();
            Collection<BindingOperation> bOperations = binding.getBindingOperations();
            PortType portType = (PortType) d.getPortTypes().toArray()[0];
            Collection<Operation> operations = portType.getOperations();
            
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            
            try {
                for (BindingOperation bOperation : bOperations) {
                    if (methodName.equals(bOperation.getName())) {
                        ProfilesModelHelper.getInstance(PolicyModelHelper.getConfigVersion(bOperation)).setMessageLevelSecurityProfilePolicies(bOperation, ComboConstants.NONE);
                        binding.removeBindingOperation(bOperation);
                    }
                }
                
                for (Operation o : operations) {
                    if (methodName.equals(o.getName())) {
                        portType.removeOperation(o);
                    }
                }
                
                for (Message m : messages) {
                    if (methodName.equals(m.getName()) || (methodName + "Response").equals(m.getName())) {
                        d.removeMessage(m);
                    }
                }
            } finally {
                if (!isTransaction) {
                    try {
                        model.endTransaction();
                    }
                    catch(IllegalStateException  e ){
                        Logger.getLogger(WsitServiceChangeListener.class.getName()).
                            log(Level.WARNING, null , e);
                    }
                }
            }
            WSITModelSupport.save(binding);
        }
    }
    
    public void operationChanged(MethodModel oldMethod, MethodModel newMethod) { }
    
}
