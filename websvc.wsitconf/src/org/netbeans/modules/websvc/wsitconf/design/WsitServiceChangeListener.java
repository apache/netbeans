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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
