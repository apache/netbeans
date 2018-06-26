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
/**
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
