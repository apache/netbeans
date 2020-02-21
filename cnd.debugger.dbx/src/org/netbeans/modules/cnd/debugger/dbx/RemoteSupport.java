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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.debugger.dbx;

import com.sun.tools.swdev.glue.Glue;
import com.sun.tools.swdev.glue.Keyring;
import com.sun.tools.swdev.glue.SecurityStyle;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.SecuritySettings;
import org.netbeans.modules.cnd.debugger.dbx.vjsch.VJSchImpl;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;

public final class RemoteSupport {

    private static SecurityStyle securityStyle;

    private static SecurityStyle securityStyle() {
	if (securityStyle == null) {
	    final String security;
	    security =  System.getProperty("cnd.nativedebugger.security",// NOI18N
					   "vjsch");			     // NOI18N

            if ("vjsch".equals(security)) { // NOI18N
                securityStyle = new SecurityStyle.VJSch();
                Glue.setVirtJSch(new VJSchImpl());
            } else if ("jsch".equals(security)) { // NOI18N
		securityStyle = new SecurityStyle.JSch();

	    } else if ("rsh".equals(security)) { // NOI18N
		securityStyle = new SecurityStyle.Rsh();

	    } else if ("ssh-cmd".equals(security)) { // NOI18N
		securityStyle = new SecurityStyle.SshCmd();

	    } else {
		securityStyle = new SecurityStyle.Rsh();
	    }

	    // Also initialize the glue keyring
	    // Always do this per bug #186828
	    Keyring keyRing = new CndKeyring();
	    Glue.setKeyring(keyRing);
	}
	return securityStyle;
    }

    /**
     * Convert dbxgui SecuritySettings to glue SecurityStyle.
     * @param ss
     * @return
     */
    public static SecurityStyle securityStyle(SecuritySettings ss) {
        SecurityStyle defSecurityStyle = securityStyle();
        if (defSecurityStyle instanceof SecurityStyle.JSch) {
            return new SecurityStyle.JSch(ss.sshPort(), ss.sshPublicKeyFile());
        } else if (defSecurityStyle instanceof SecurityStyle.VJSch) {
            return new SecurityStyle.VJSch(ss.sshPort());
        } else {    // Allow overriding using properties
            return defSecurityStyle;
        }
    }

    public static final class CndKeyring implements Keyring {

	public CndKeyring() {
	    // A single instance is registered in RemoteSupport.securityStyle().
	}

	public String getPassword(String hostName, String userName, SecurityStyle ss) {
	    if (ss instanceof SecurityStyle.JSch) {
		// Delegate to CND's "keyring" aka PasswordManager.
		// The map key for PasswordManager is an ExecutionEnvironment
		SecurityStyle.JSch ssj = (SecurityStyle.JSch) ss;
		PasswordManager pm = PasswordManager.getInstance();
		ExecutionEnvironment key =
		    ExecutionEnvironmentFactory.createNew(userName,
		                                          hostName,
							  ssj.sshPort());
		char [] passwdArray = pm.getPassword(key);
		if (passwdArray == null)
		    return null;
	        String passwd = new String(passwdArray);
		return passwd;
	    } else {
		throw new UnsupportedOperationException("Keyring only supported for jsch"); // NOI18N
	    }
	}

	public void setPassword(String hostName, String userName, SecurityStyle ss, String password, boolean rememberPassword) {
	    if (ss instanceof SecurityStyle.JSch) {
		SecurityStyle.JSch ssj = (SecurityStyle.JSch) ss;
		PasswordManager pm = PasswordManager.getInstance();
		ExecutionEnvironment key =
		    ExecutionEnvironmentFactory.createNew(userName,
		                                          hostName,
							  ssj.sshPort());
		pm.storePassword(key, password.toCharArray(), rememberPassword);

	    } else {
		throw new UnsupportedOperationException("Not supported yet."); // NOI18N
	    }
	}

	public boolean isRememberPassword(String hostName, String userName, SecurityStyle ss) {
	    if (ss instanceof SecurityStyle.JSch) {
		SecurityStyle.JSch ssj = (SecurityStyle.JSch) ss;
		PasswordManager pm = PasswordManager.getInstance();
		ExecutionEnvironment key =
		    ExecutionEnvironmentFactory.createNew(userName,
		                                          hostName,
							  ssj.sshPort());
		return pm.isRememberPassword(key);

	    } else {
		throw new UnsupportedOperationException("Not supported yet."); // NOI18N
	    }
	}
    }
}
