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
