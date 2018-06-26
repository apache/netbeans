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

package org.netbeans.modules.websvc.jaxws.api;

import java.util.HashMap;
import java.util.Map;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** ConteHandler that gives information if wsdl wrapper need to be created
 *  This is the case when service element is missing
 *
 * @author mkuchtiak
 */
public class WsdlWrapperHandler extends DefaultHandler{
    
    public static final String WSDL_SOAP_URI = "http://schemas.xmlsoap.org/wsdl/"; //NOI18N
    public static final String SOAP_BINDING_PREFIX = "http://schemas.xmlsoap.org/wsdl/soap"; //NOI18N
    
    private boolean isService, isPortType, isBinding;
    private String tns;
    private Map<String, String> prefixes;
    private Map<String, BindingInfo> bindings;
    private Map<String, String> ports;
    private BindingInfo bindingInfo;
    private boolean insideBinding, insideService;
    
    /** Creates a new instance of WsdlWrapperHandler */
    public WsdlWrapperHandler() {
        prefixes = new HashMap<String, String>();
        bindings = new HashMap<String, BindingInfo>();
        ports = new HashMap<String, String>();
    }    

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (!prefixes.containsKey(uri)) prefixes.put(uri,prefix);
    }
    
    public void startElement(String uri, String localName, String qname, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
        if("portType".equals(localName)) { // NOI18N
            isPortType=true;
        } else if("binding".equals(localName)) { // NOI18N
            isBinding=true;
            if (WSDL_SOAP_URI.equals(uri)) {
                String bindingName=attributes.getValue("name"); // NOI18N
                insideBinding=true;
                if (bindingName!=null) {
                    bindingInfo = new BindingInfo(bindingName);
                    bindings.put(bindingName,bindingInfo);
                }
            } else if (insideBinding && bindingInfo!=null && uri.startsWith(SOAP_BINDING_PREFIX)) {
                bindingInfo.setBindingType(uri);
            }
        } else if("service".equals(localName)) { // NOI18N
            isService=true;
            insideService=true;
        } else if("port".equals(localName) && insideService) { // NOI18N
            String portName = attributes.getValue("name"); // NOI18N
            if (portName!=null) ports.put(portName, attributes.getValue("binding")); // NOI18N
        } else if("definitions".equals(localName)) { // NOI18N
            tns=attributes.getValue("targetNamespace"); // NOI18N
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if("binding".equals(localName) && WSDL_SOAP_URI.equals(uri)) { // NOI18N
            bindingInfo=null;
            insideBinding=false;
        } else if ("service".equals(localName)) {
            insideService=false;
        }
    }
    
    public String getBindingTypeForPort(String name) {
        String fullBindingName = ports.get(name);
        if (fullBindingName!=null) {
            String bindingName = getLocalPart(fullBindingName);
            BindingInfo info = bindings.get(bindingName);
            if (info!=null) return info.getBindingType();
        }
        return null;
    }
    
    public boolean isServiceElement() {
        return isService;
    }
    
    public String getTargetNsPrefix() {
        return (prefixes == null) ? null : prefixes.get(tns);
    }

    public void endDocument() throws SAXException {
        // throw exception if service & binding & portType are missing 
        if (!isService && !isBinding && !isPortType) throw new SAXException("Missing wsdl elements (wsdl:service | wsdl:binding | wsdl:portType)"); //NOI18N
    }
    
    private class BindingInfo {
        private String bindingName;
        private String bindingType;
        
        BindingInfo(String bindingName) {
            this.bindingName=bindingName;
        }
        
        void setBindingType(String bindingType) {
            this.bindingType=bindingType;
        }
        
        String getBindingType() {
            return bindingType;
        }
    }
    
    private String getLocalPart(String fullName) {
        int index = fullName.indexOf(":"); //NOI18N
        return (index>=0?fullName.substring(index+1):fullName);
    }
}
