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

