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
 * SecurityMap.java
 *
 * Created on November 21, 2004, 2:33 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface SecurityMap {

        public static final String NAME = "Name";	// NOI18N
	public static final String PRINCIPAL = "Principal";	// NOI18N
	public static final String USER_GROUP = "UserGroup";	// NOI18N
	public static final String BACKEND_PRINCIPAL = "BackendPrincipal";	// NOI18N
	public static final String BACKENDPRINCIPALUSERNAME = "BackendPrincipalUserName";	// NOI18N
	public static final String BACKENDPRINCIPALPASSWORD = "BackendPrincipalPassword";	// NOI18N
        
	public void setName(java.lang.String value);

	public java.lang.String getName();

	public void setPrincipal(int index, String value);

	public String getPrincipal(int index);

	public int sizePrincipal();

	public void setPrincipal(String[] value);

	public String[] getPrincipal();

	public int addPrincipal(String value);

	public int removePrincipal(String value);

	public void setUserGroup(int index, String value);

	public String getUserGroup(int index);

	public int sizeUserGroup();

	public void setUserGroup(String[] value);

	public String[] getUserGroup();

	public int addUserGroup(String value);

	public int removeUserGroup(String value);

	public void setBackendPrincipal(boolean value);

	public boolean isBackendPrincipal();

	public void setBackendPrincipalUserName(java.lang.String value);

	public java.lang.String getBackendPrincipalUserName();

	public void setBackendPrincipalPassword(java.lang.String value);

	public java.lang.String getBackendPrincipalPassword();

}
