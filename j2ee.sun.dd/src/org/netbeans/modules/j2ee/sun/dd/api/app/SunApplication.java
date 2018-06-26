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
 * SunApplication.java
 *
 * Created on November 21, 2004, 12:47 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.app;

import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;

public interface SunApplication extends org.netbeans.modules.j2ee.sun.dd.api.RootInterface {

        public static final String VERSION_6_0_1 = "6.01"; //NOI18N
        public static final String VERSION_6_0_0 = "6.00"; //NOI18N
        public static final String VERSION_5_0_0 = "5.00"; //NOI18N
        public static final String VERSION_1_4_0 = "1.40"; //NOI18N
        public static final String VERSION_1_3_0 = "1.30"; //NOI18N
        
        public static final String WEB = "Web";	// NOI18N
	public static final String PASS_BY_REFERENCE = "PassByReference";	// NOI18N
	public static final String UNIQUE_ID = "UniqueId";	// NOI18N
	public static final String SECURITY_ROLE_MAPPING = "SecurityRoleMapping";	// NOI18N
	public static final String REALM = "Realm";	// NOI18N
        
	public void setWeb(int index, Web value);
	public Web getWeb(int index);
	public int sizeWeb();
	public void setWeb(Web[] value);
	public Web[] getWeb();
	public int addWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web value);
	public int removeWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web value);
	public Web newWeb();

        /** Setter for pass-by-reference property
        * @param value property value
        */
	public void setPassByReference(String value);
        /** Getter for pass-by-reference property.
        * @return property value
        */
	public String getPassByReference();
        /** Setter for unique-id property
        * @param value property value
        */
	public void setUniqueId(String value);
        /** Getter for unique-id property.
        * @return property value
        */
	public String getUniqueId();

	public void setSecurityRoleMapping(int index, SecurityRoleMapping value);
	public SecurityRoleMapping getSecurityRoleMapping(int index);
	public int sizeSecurityRoleMapping();
	public void setSecurityRoleMapping(SecurityRoleMapping[] value);
	public SecurityRoleMapping[] getSecurityRoleMapping();
	public int addSecurityRoleMapping(SecurityRoleMapping value);
	public int removeSecurityRoleMapping(SecurityRoleMapping value);
	public SecurityRoleMapping newSecurityRoleMapping();

        /** Setter for realm property
        * @param value property value
        */
	public void setRealm(String value);
        /** Getter for realm property.
        * @return property value
        */
	public String getRealm();

}
