
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
/*
 * CustomizationComponentFactory.java
 *
 * Created on March 24, 2006, 11:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.customization.model;

import org.netbeans.modules.websvc.customization.model.impl.BindingCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.BindingOperationCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.DefinitionsCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.EnableAsyncMappingImpl;
import org.netbeans.modules.websvc.customization.model.impl.EnableMIMEContentImpl;
import org.netbeans.modules.websvc.customization.model.impl.EnableWrapperStyleImpl;
import org.netbeans.modules.websvc.customization.model.impl.JavaClassImpl;
import org.netbeans.modules.websvc.customization.model.impl.JavaDocImpl;
import org.netbeans.modules.websvc.customization.model.impl.JavaExceptionImpl;
import org.netbeans.modules.websvc.customization.model.impl.JavaMethodImpl;
import org.netbeans.modules.websvc.customization.model.impl.JavaPackageImpl;
import org.netbeans.modules.websvc.customization.model.impl.JavaParameterImpl;
import org.netbeans.modules.websvc.customization.model.impl.PortCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.PortTypeCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.PortTypeOperationCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.PortTypeOperationFaultCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.ProviderImpl;
import org.netbeans.modules.websvc.customization.model.impl.ServiceCustomizationImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author rico
 */
public class CustomizationComponentFactory {
    
    private static CustomizationComponentFactory factory =
            new CustomizationComponentFactory();
    /** Creates a new instance of CustomizationComponentFactory */
    private CustomizationComponentFactory() {
    }
    
    public static CustomizationComponentFactory getDefault(){
        return factory;
    }
    
    public BindingCustomization createBindingCustomization(WSDLModel model){
        return new BindingCustomizationImpl(model);
    }
    
    public BindingOperationCustomization createBindingOperationCustomization(WSDLModel model){
        return new BindingOperationCustomizationImpl(model);
    }
    
    public DefinitionsCustomization createDefinitionsCustomization(WSDLModel model){
        return new DefinitionsCustomizationImpl(model);
    }
    
    public EnableAsyncMapping createEnableAsyncMapping(WSDLModel model){
        return new EnableAsyncMappingImpl(model);
    }
    
    public EnableMIMEContent createEnableMIMEContent(WSDLModel model){
        return new EnableMIMEContentImpl(model);
    }
    
    public EnableWrapperStyle createEnableWrapperStyle(WSDLModel model){
        return new EnableWrapperStyleImpl(model);
    }
    
    public JavaClass createJavaClass(WSDLModel model){
        return new JavaClassImpl(model);
    }
    
    public JavaDoc createJavaDoc(WSDLModel model){
        return new JavaDocImpl(model);
    }
    
    public JavaException createJavaException(WSDLModel model){
        return new JavaExceptionImpl(model);
    }
    
    public JavaMethod createJavaMethod(WSDLModel model){
        return new JavaMethodImpl(model);
    }
    
    public JavaPackage createJavaPackage(WSDLModel model){
        return new JavaPackageImpl(model);
    }
    
    public JavaParameter createJavaParameter(WSDLModel model){
        return new JavaParameterImpl(model);
    }
    
    public PortCustomization createPortCustomization(WSDLModel model){
        return new PortCustomizationImpl(model);
    }
    
    public PortTypeCustomization createPortTypeCustomization(WSDLModel model){
        return new PortTypeCustomizationImpl(model);
    }
    
    public PortTypeOperationCustomization createPortTypeOperationCustomization(WSDLModel model){
        return new PortTypeOperationCustomizationImpl(model);
    }
    
    public PortTypeOperationFaultCustomization createPortTypeOperationFaultCustomization(WSDLModel model){
        return new PortTypeOperationFaultCustomizationImpl(model);
    }
    
    public Provider createProvider(WSDLModel model){
        return new ProviderImpl(model);
    }
    
    public ServiceCustomization createServiceCustomization(WSDLModel model){
        return new ServiceCustomizationImpl(model);
    }
}
