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
package org.netbeans.modules.j2ee.dd.api.ejb;

/**
 * Generated interface for EjbJar element.<br>
 * The EjbJar object is the root of bean graph generated<br>
 * for deployment descriptor(ejb-jar.xml) file.<br>
 * For getting the root (EjbJar object) use the {@link DDProvider#getDDRoot} method.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface EjbJar extends org.netbeans.modules.j2ee.dd.api.common.RootInterface {
    public static final String PROPERTY_VERSION="dd_version"; //NOI18N
    public static final String VERSION_2_1="2.1"; //NOI18N
    public static final String VERSION_3_0="3.0"; //NOI18N
    public static final String VERSION_3_1="3.1"; //NOI18N

    /**
     * ejb-jar.xml DD version for JavaEE7
     * @since 1.29
     */
    public static final String VERSION_3_2 = "3.2"; //NOI18N
    public static final String VERSION_4_0 = "4.0"; //NOI18N
    public static final int STATE_VALID=0;
    public static final int STATE_INVALID_PARSABLE=1;
    public static final int STATE_INVALID_UNPARSABLE=2;
    public static final String PROPERTY_STATUS="dd_status"; //NOI18N
    
    public static final String ENTERPRISE_BEANS = "EnterpriseBeans";	// NOI18N
    public static final String RELATIONSHIPS = "Relationships";	// NOI18N
    public static final String ASSEMBLY_DESCRIPTOR = "AssemblyDescriptor";	// NOI18N
    public static final String EJB_CLIENT_JAR = "EjbClientJar";	// NOI18N

    /** Setter for version property.
     * Warning : Only the upgrade from lower to higher version is supported.
     * @param version ejb-jar version value
     */
    public void setVersion(java.math.BigDecimal version);
    /** Getter for version property.
     * @return property value
     */
    public java.math.BigDecimal getVersion();
    /** Getter for SAX Parse Error property.
     * Used when deployment descriptor is in invalid state.
     * @return property value or null if in valid state
     */
    public org.xml.sax.SAXParseException getError();
    /** Getter for status property.
     * @return property value
     */
    public int getStatus();
    
    public void setEnterpriseBeans(EnterpriseBeans value);

    public EnterpriseBeans getEnterpriseBeans();
    
    public EnterpriseBeans newEnterpriseBeans();
    
    public void setRelationships(Relationships value);
    
    public Relationships getSingleRelationships();
    
    public Relationships newRelationships();
    
    public void setAssemblyDescriptor(AssemblyDescriptor value);
    
    public AssemblyDescriptor getSingleAssemblyDescriptor();
    
    public AssemblyDescriptor newAssemblyDescriptor();
    
    public void setEjbClientJar(String value);
    
    public String getSingleEjbClientJar();  

    // EJB 3.0
    
    void setInterceptors(Interceptors valueInterface) throws VersionNotSupportedException;
    Interceptors getInterceptors() throws VersionNotSupportedException;
    Interceptors newInterceptors() throws VersionNotSupportedException;
    
}

