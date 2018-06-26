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

package org.netbeans.modules.j2ee.dd.api.application;

import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface Application extends org.netbeans.modules.j2ee.dd.api.common.RootInterface {
	
        public static final String MODULE = "Module";	// NOI18N
	public static final String SECURITY_ROLE = "SecurityRole";	// NOI18N

        public static final String PROPERTY_VERSION="dd_version"; //NOI18N
        public static final String VERSION_1_4="1.4"; //NOI18N
        public static final String VERSION_5="5"; //NOI18N
        public static final String VERSION_6="6"; //NOI18N

        /**
         * application.xml DD version for JavaEE7
         * @since 1.29
         */
        public static final String VERSION_7 = "7"; //NOI18N
        public static final int STATE_VALID=0;
        public static final int STATE_INVALID_PARSABLE=1;
        public static final int STATE_INVALID_UNPARSABLE=2;
        public static final String PROPERTY_STATUS="dd_status"; //NOI18N
    
        //public void setVersion(java.lang.String value);
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
    
	public void setModule(int index, Module value);

	public Module getModule(int index);

	public int sizeModule();

	public void setModule(Module[] value);

	public Module[] getModule();

	public int addModule(org.netbeans.modules.j2ee.dd.api.application.Module value);

	public int removeModule(org.netbeans.modules.j2ee.dd.api.application.Module value);

	public Module newModule();

	public void setSecurityRole(int index, org.netbeans.modules.j2ee.dd.api.common.SecurityRole value);

	public org.netbeans.modules.j2ee.dd.api.common.SecurityRole getSecurityRole(int index);

	public int sizeSecurityRole();

	public void setSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] value);

	public org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] getSecurityRole();

	public int addSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value);

	public int removeSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value);

	public org.netbeans.modules.j2ee.dd.api.common.SecurityRole newSecurityRole();

        
        //1.4
        public void setIcon(int index, org.netbeans.modules.j2ee.dd.api.common.Icon value) throws VersionNotSupportedException;

	public org.netbeans.modules.j2ee.dd.api.common.Icon getIcon(int index) throws VersionNotSupportedException;

	public int sizeIcon() throws VersionNotSupportedException;

	public void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon[] value) throws VersionNotSupportedException;

	//public org.netbeans.modules.j2ee.dd.api.common.Icon[] getIcon() throws VersionNotSupportedException;

	public int addIcon(org.netbeans.modules.j2ee.dd.api.common.Icon value)  throws VersionNotSupportedException;

	public int removeIcon(org.netbeans.modules.j2ee.dd.api.common.Icon value) throws VersionNotSupportedException;

	public org.netbeans.modules.j2ee.dd.api.common.Icon newIcon(); 

}

