/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * This interface has all of the bean info accessor methods.
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.dd.api.webservices;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface PortComponent extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean{
	
    public static final String PORT_COMPONENT_NAME = "PortComponentName";	// NOI18N
	public static final String PORTCOMPONENTNAMEID = "PortComponentNameId";	// NOI18N
	public static final String WSDL_PORT = "WsdlPort";	// NOI18N
	public static final String WSDLPORTID = "WsdlPortId";	// NOI18N
	public static final String SERVICE_ENDPOINT_INTERFACE = "ServiceEndpointInterface";	// NOI18N
	public static final String SERVICE_IMPL_BEAN = "ServiceImplBean";	// NOI18N
	public static final String HANDLER = "Handler";	// NOI18N
	
	public void setId(java.lang.String value);

	public java.lang.String getId();

	public void setDescription(java.lang.String value);

	public java.lang.String getDescription();

	public void setDescriptionId(java.lang.String value);

	public java.lang.String getDescriptionId();

	public void setDescriptionXmlLang(java.lang.String value);

	public java.lang.String getDescriptionXmlLang();

	public void setDisplayName(java.lang.String value);

	public java.lang.String getDisplayName();

	public void setDisplayNameId(java.lang.String value);

	public java.lang.String getDisplayNameId();

	public void setDisplayNameXmlLang(java.lang.String value);

	public java.lang.String getDisplayNameXmlLang();

	public void setIcon(Icon value);

	public Icon getIcon();

	public Icon newIcon();


	public void setPortComponentName(java.lang.String value);

	public java.lang.String getPortComponentName();

	public void setPortComponentNameId(java.lang.String value);

	public java.lang.String getPortComponentNameId();

	//public void setWsdlPort(java.lang.String value);

	//public java.lang.String getWsdlPort();

        public void setWsdlService(javax.xml.namespace.QName value) throws VersionNotSupportedException;

	public javax.xml.namespace.QName getWsdlService() throws VersionNotSupportedException;

	public void setWsdlServiceId(java.lang.String value) throws VersionNotSupportedException;

	public java.lang.String getWsdlServiceId() throws VersionNotSupportedException;
        
        public void setWsdlPort(javax.xml.namespace.QName value);

	public javax.xml.namespace.QName getWsdlPort();

	public void setWsdlPortId(java.lang.String value);

	public java.lang.String getWsdlPortId();

	public void setServiceEndpointInterface(java.lang.String value);

	public java.lang.String getServiceEndpointInterface();

	public void setServiceImplBean(ServiceImplBean value);

	public ServiceImplBean getServiceImplBean();

	public ServiceImplBean newServiceImplBean();

	public void setHandler(int index, PortComponentHandler value);

	public PortComponentHandler getHandler(int index);

	public int sizeHandler();

	public void setHandler(PortComponentHandler[] value);

	public PortComponentHandler[] getHandler();

	public int addHandler(org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler value);

	public int removeHandler(org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler value);

	public PortComponentHandler newPortComponentHandler();

}
