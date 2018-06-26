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

package org.netbeans.modules.j2ee.dd.api.common;
/**
 * Generated interface for ServiceRef element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface ServiceRef extends ComponentInterface {

        public static final String SERVICE_REF_NAME = "ServiceRefName";	// NOI18N
	public static final String SERVICE_INTERFACE = "ServiceInterface";	// NOI18N
	public static final String WSDL_FILE = "WsdlFile";	// NOI18N
	public static final String JAXRPC_MAPPING_FILE = "JaxrpcMappingFile";	// NOI18N
	public static final String SERVICE_QNAME = "ServiceQname";	// NOI18N
	public static final String PORT_COMPONENT_REF = "PortComponentRef";	// NOI18N
	public static final String HANDLER = "Handler";	// NOI18N
        /** Setter for service-ref-name property.
         * @param value property value
         */
	public void setServiceRefName(String value);
        /** Getter for service-ref-name property.
         * @return property value 
         */
	public String getServiceRefName();
        /** Setter for service-interface property.
         * @param value property value
         */
	public void setServiceInterface(String value);
        /** Getter for service-interface property.
         * @return property value 
         */
	public String getServiceInterface();
        /** Setter for wsdl-file property.
         * @param value property value
         */
	public void setWsdlFile(java.net.URI value);
        /** Getter for wsdl-file property.
         * @return property value 
         */
	public java.net.URI getWsdlFile();
        /** Setter for jaxrpc-mapping-file property.
         * @param value property value
         */
	public void setJaxrpcMappingFile(String value);
        /** Getter for jaxrpc-mapping-file property.
         * @return property value 
         */
	public String getJaxrpcMappingFile();
        /** Setter for service-qname property.
         * @param value property value
         */
	public void setServiceQname(String value);
        /** Getter for service-qname property.
         * @return property value 
         */
	public String getServiceQname();
        /** Setter for port-component-ref element.
         * @param index position in the array of elements
         * @param valueInterface port-component-ref element (PortComponentRef object)
         */
	public void setPortComponentRef(int index, PortComponentRef valueInterface);
        /** Getter for port-component-ref element.
         * @param index position in the array of elements
         * @return port-component-ref element (PortComponentRef object)
         */
	public PortComponentRef getPortComponentRef(int index);
        /** Setter for port-component-ref elements.
         * @param value array of port-component-ref elements (PortComponentRef objects)
         */
	public void setPortComponentRef(PortComponentRef[] value);
        /** Getter for port-component-ref elements.
         * @return array of port-component-ref elements (PortComponentRef objects)
         */
	public PortComponentRef[] getPortComponentRef();
        /** Returns size of port-component-ref elements.
         * @return number of port-component-ref elements 
         */
	public int sizePortComponentRef();
        /** Adds port-component-ref element.
         * @param valueInterface port-component-ref element (PortComponentRef object)
         * @return index of new port-component-ref
         */
	public int addPortComponentRef(PortComponentRef valueInterface);
        /** Removes port-component-ref element.
         * @param valueInterface port-component-ref element (PortComponentRef object)
         * @return index of the removed port-component-ref
         */
	public int removePortComponentRef(PortComponentRef valueInterface);
        /** Setter for handler element.
         * @param index position in the array of elements
         * @param valueInterface handler element (SeviceRefHandler object)
         */
	public void setHandler(int index, ServiceRefHandler valueInterface);
        /** Getter for handler element.
         * @param index position in the array of elements
         * @return handler element (SeviceRefHandler object)
         */
	public ServiceRefHandler getHandler(int index);
        /** Setter for handler elements.
         * @param value array of handler elements (SeviceRefHandler objects)
         */
	public void setHandler(ServiceRefHandler[] value);
        /** Getter for handler elements.
         * @return array of handler elements (SeviceRefHandler objects)
         */
	public ServiceRefHandler[] getHandler();
        /** Returns size of handler elements.
         * @return number of handler elements 
         */
	public int sizeHandler();
        /** Adds handler element.
         * @param valueInterface handler element (SeviceRefHandler object)
         * @return index of new handler
         */
	public int addHandler(ServiceRefHandler valueInterface);
        /** Removes handler element.
         * @param valueInterface handler element (SeviceRefHandler object)
         * @return index of the removed handler
         */
	public int removeHandler(ServiceRefHandler valueInterface);

        // Java EE 5
        
	void setMappedName(String value) throws VersionNotSupportedException;
	String getMappedName() throws VersionNotSupportedException;
	void setHandlerChains(ServiceRefHandlerChains valueInterface) throws VersionNotSupportedException;
	ServiceRefHandlerChains getHandlerChains() throws VersionNotSupportedException;
	PortComponentRef newPortComponentRef() throws VersionNotSupportedException;
	ServiceRefHandler newServiceRefHandler() throws VersionNotSupportedException;
	ServiceRefHandlerChains newServiceRefHandlerChains() throws VersionNotSupportedException;

}
