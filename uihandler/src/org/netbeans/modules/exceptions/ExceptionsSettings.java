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
package org.netbeans.modules.exceptions;

import java.awt.EventQueue;
import java.util.prefs.Preferences;
import org.netbeans.api.keyring.Keyring;
import org.openide.util.NbBundle;

/**
 *
 * @author Jindrich Sedek
 */
public class ExceptionsSettings {

    private static final String userProp = "UserName";       // NOI18N
    private static final String passwdProp = "Passwd";
    private static final String passwdKey = "exceptionreporter"; // NOI18N
    private static final String guestProp = "Guest";
    private static final String rememberProp = "RememberPasswd";
    private String userName;
    private char[] passwd;
    private boolean changed = false;

    /** Creates a new instance of ExceptionsSettings */
    public ExceptionsSettings() {
        assert !EventQueue.isDispatchThread();
        userName = prefs().get(userProp, "");
        String old = prefs().get(passwdProp, null);
        char[] keyringPasswd = Keyring.read(passwdKey);
        if (old != null) {
            passwd = old.toCharArray();
            changed = true;
            prefs().remove(passwdProp);
        }else if (keyringPasswd != null) {
            passwd = keyringPasswd;
        }
        if (passwd == null){
            passwd = new char[0];
        }
        if (userName == null){
            userName = new String();
        }
    }

    public void save(){
        assert !EventQueue.isDispatchThread();
        if (!changed){
            return;
        }
        prefs().put(userProp, userName);
        Keyring.save(passwdKey, passwd,
                NbBundle.getMessage(ExceptionsSettings.class, "ExceptionsSettings.password.description"));
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        changed = true;
    }

    public char[] getPasswd() {
        return passwd;
    }

    public void setPasswd(char[] passwd) {
        changed = true;
        this.passwd = passwd;
    }

    public boolean isGuest() {
        return prefs().getBoolean(guestProp, false);
    }

    public void setGuest(Boolean guest) {
        prefs().putBoolean(guestProp, guest);
    }

    public boolean rememberPasswd() {
        return prefs().getBoolean(rememberProp, true);
    }

    public void setRememberPasswd(boolean remember) {
        prefs().putBoolean(rememberProp, remember);
    }

    private Preferences prefs() {
        return java.util.prefs.Preferences.userRoot().node("org/netbeans/modules/exceptions");
    }
}
