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
/**
 * This interface has all of the bean info accessor methods.
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.dd.api.webservices;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;

public interface PortComponentHandler extends org.netbeans.modules.j2ee.dd.api.common.ComponentInterface {
	
        public Icon newIcon();

	public void setHandlerName(java.lang.String value);

	public java.lang.String getHandlerName();

	public void setHandlerNameId(java.lang.String value);

	public java.lang.String getHandlerNameId();

	public void setHandlerClass(java.lang.String value);

	public java.lang.String getHandlerClass();

	public void setInitParam(int index, InitParam value);

	public InitParam getInitParam(int index);

	public int sizeInitParam();

	public void setInitParam(InitParam[] value);

	public InitParam[] getInitParam();

	public int addInitParam(InitParam value);

	public int removeInitParam(InitParam value);

	public InitParam newInitParam();

	public void setSoapHeader(int index, javax.xml.namespace.QName value);

	public javax.xml.namespace.QName getSoapHeader(int index);

	public int sizeSoapHeader();

	public void setSoapHeader(javax.xml.namespace.QName[] value);

	public javax.xml.namespace.QName[] getSoapHeader();

	public int addSoapHeader(javax.xml.namespace.QName value);

	public int removeSoapHeader(javax.xml.namespace.QName value);

	public void setSoapHeaderId(int index, java.lang.String value);

	public java.lang.String getSoapHeaderId(int index);

	public int sizeSoapHeaderId();

	public void setSoapRole(int index, java.lang.String value);

	public java.lang.String getSoapRole(int index);

	public int sizeSoapRole();

	public void setSoapRole(java.lang.String[] value);

	public java.lang.String[] getSoapRole();

	public int addSoapRole(java.lang.String value);

	public int removeSoapRole(java.lang.String value);

	public void setSoapRoleId(java.lang.String value);

	public java.lang.String getSoapRoleId();

}
