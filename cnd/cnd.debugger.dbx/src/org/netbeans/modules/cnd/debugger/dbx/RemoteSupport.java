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
