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
package org.netbeans.modules.websvc.saas.codegen.j2ee.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author nam
 */
public class SoapClientUtils {

    private static Map<String,String> xsdToJavaTypes = null;
    public static String toJavaType(String xsdType) {
        if ( xsdToJavaTypes == null) {
            xsdToJavaTypes = new HashMap<String,String>();
            xsdToJavaTypes.put("string", "java.lang.String"); //NOI18N
            xsdToJavaTypes.put("boolean", "java.lang.Boolean"); //NOI18N
            xsdToJavaTypes.put("date", "java.sql.Date"); //NOI18N
            xsdToJavaTypes.put("dateTime", "java.sql.Date"); //NOI18N
            xsdToJavaTypes.put("time", "java.sql.Time"); //NOI18N
            xsdToJavaTypes.put("token", "java.lang.String"); //NOI18N
            xsdToJavaTypes.put("double", "java.lang.Double"); //NOI18N
            xsdToJavaTypes.put("float", "java.lang.Float"); //NOI18N
            xsdToJavaTypes.put("byte", "java.lang.Byte"); //NOI18N
            xsdToJavaTypes.put("int", "java.lang.Integer"); //NOI18N
            xsdToJavaTypes.put("long", "java.lang.Long"); //NOI18N
            xsdToJavaTypes.put("short", "java.lang.Short"); //NOI18N
            xsdToJavaTypes.put("unsignedByte", "java.lang.Byte"); //NOI18N
            xsdToJavaTypes.put("unsignedInt", "java.lang.Integer"); //NOI18N
            xsdToJavaTypes.put("unsignedLong", "java.lang.Long"); //NOI18N
            xsdToJavaTypes.put("unsignedShort", "java.lang.Short"); //NOI18N
            xsdToJavaTypes.put("nonNegativeInteger", "java.lang.Integer"); //NOI18N
            xsdToJavaTypes.put("nonPositiveInteger", "java.lang.Integer"); //NOI18N
            xsdToJavaTypes.put("negativeInteger", "java.lang.Integer"); //NOI18N
            xsdToJavaTypes.put("positiveInteger", "java.lang.Integer"); //NOI18N
        }
        return xsdToJavaTypes.get(xsdType);
    }

    public static Map<QName,String> getSoapHandlerParameters(WSDLModel model, 
                                                             WSPort wsPort,
                                                             WSOperation wsOperation) {
        Map<QName,String> paramMap = new HashMap<QName,String>();

        QName portQName = new QName(wsPort.getNamespaceURI(), wsPort.getName());
        Binding binding = null;
        for(Binding b : model.getDefinitions().getBindings()){
            if (b.getType().getQName().equals(portQName)) {
                binding = b;
                break;
            }
        }
        if (binding == null) {
            return paramMap;
        }

        List<SOAPBinding> soapBindings = binding.getExtensibilityElements(SOAPBinding.class);
        if (soapBindings.isEmpty()) {
            return paramMap;
        }

        BindingOperation bindingOperation = null;
        for (BindingOperation bOp : binding.getBindingOperations()) {
            if (bOp.getOperation().get().getName().equals(wsOperation.getOperationName())) {
                bindingOperation = bOp;
                break;
            }
        }
        if (bindingOperation == null) {
            return paramMap;
        }
        
        BindingInput bindingInput = bindingOperation.getBindingInput();
        for (SOAPHeader header : bindingInput.getExtensibilityElements(SOAPHeader.class)) {
            if (header.getPartRef() == null) {
                continue;
            }
            Part part = header.getPartRef().get();
            if (part != null && part.getElement() != null) {
                GlobalElement element = part.getElement().get();
                paramMap.put(part.getElement().getQName(), guessSimpleJavaType(element));
            }
        }
        
        return paramMap;
    }

    public static String guessSimpleJavaType(GlobalElement element) {
        String xsdType = null;
        String javaType = Object.class.getName();
        if (element.getType() != null && 
            element.getType().getEffectiveNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
            xsdType = element.getType().get().getName();
        } else if (element.getInlineType() instanceof LocalSimpleType) {
            LocalSimpleType lst = (LocalSimpleType) element.getInlineType();
            if (lst.getDefinition() instanceof SimpleTypeRestriction) {
                NamedComponentReference<GlobalSimpleType> ref = ((SimpleTypeRestriction)lst.getDefinition()).getBase();
                if (ref.getEffectiveNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
                    xsdType = ref.get().getName();
                }
            }
        }
        if (xsdType != null) {
            javaType = toJavaType(xsdType);
        }
        return javaType;
    }
}
