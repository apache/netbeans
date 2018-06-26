
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
