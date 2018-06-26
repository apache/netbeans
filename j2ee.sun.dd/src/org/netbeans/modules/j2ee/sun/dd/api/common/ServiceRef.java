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
 * ServiceRef.java
 *
 * Created on November 17, 2004, 5:09 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface ServiceRef extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String SERVICE_REF_NAME = "ServiceRefName";	// NOI18N
    public static final String PORT_INFO = "PortInfo";	// NOI18N
    public static final String CALL_PROPERTY = "CallProperty";	// NOI18N
    public static final String WSDL_OVERRIDE = "WsdlOverride";	// NOI18N
    public static final String SERVICE_IMPL_CLASS = "ServiceImplClass";	// NOI18N
    public static final String SERVICE_QNAME = "ServiceQname";	// NOI18N
        
    /** Setter for service-ref-name property
     * @param value property value
     */
    public void setServiceRefName(java.lang.String value);
    /** Getter for service-ref-name property.
     * @return property value
     */
    public java.lang.String getServiceRefName();
    
    public PortInfo[] getPortInfo(); 
    public PortInfo getPortInfo(int index);
    public void setPortInfo(PortInfo[] value);
    public void setPortInfo(int index, PortInfo value);
    public int addPortInfo(PortInfo value);
    public int removePortInfo(PortInfo value);
    public int sizePortInfo();
    public PortInfo newPortInfo();
    
    public CallProperty[] getCallProperty(); 
    public CallProperty getCallProperty(int index);
    public void setCallProperty(CallProperty[] value);
    public void setCallProperty(int index, CallProperty value);
    public int addCallProperty(CallProperty value);
    public int removeCallProperty(CallProperty value);
    public int sizeCallProperty(); 
    public CallProperty newCallProperty(); 
    
    public void setWsdlOverride(java.lang.String value);
    public java.lang.String getWsdlOverride();
    
    public void setServiceImplClass(java.lang.String value);
    public java.lang.String getServiceImplClass();
    
    public void setServiceQname(ServiceQname value);
    public ServiceQname getServiceQname();
    public ServiceQname newServiceQname();
}
