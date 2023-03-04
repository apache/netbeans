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

package org.netbeans.modules.bugtracking.commons;

import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.netbeans.api.keyring.Keyring;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public final class NBBugzillaUtils {
    private static final String NB_BUGZILLA_PASSWORD = "nbbugzilla.password";                // NOI18N
    private static final String NB_BUGZILLA_USERNAME = "nbbugzilla.username";                // NOI18N
    
    private static final Pattern netbeansUrlPattern = Pattern.compile("(https|http)://(([a-z]|\\d)+\\.)*([a-z]|\\d)*netbeans([a-z]|\\d)*(([a-z]|\\d)*\\.)+org(.*)"); // NOI18N
    /**
     * Determines whether the given {@link RepositoryProvider} is the
     * repository hosting netbeans or not
     *
     * @param url
     * @return true if the given repository is the netbeans bugzilla, otherwise false
     */
    public static boolean isNbRepository(String url) {
        boolean ret = netbeansUrlPattern.matcher(url).matches();
        if(ret) {
            return true;
        }
        String nbUrl = System.getProperty("netbeans.bugzilla.url");  // NOI18N
        if(nbUrl == null || nbUrl.equals("")) {                      // NOI18N
            return false;
        }
        return url.startsWith(nbUrl);
    }
    
    /**
     * Returns the netbeans.org username
     * Shouldn't be called in awt
     *
     * @return username
     */
    public static String getNBUsername() {
        String user = getPreferences().get(NB_BUGZILLA_USERNAME, ""); // NOI18N
        return user.equals("") ? null : user;                                   // NOI18N
    }

    /**
     * Returns the netbeans.org password
     * Shouldn't be called in awt
     *
     * @return password
     */
    public static char[] getNBPassword() {
        return Keyring.read(NB_BUGZILLA_PASSWORD);
    }
    
    /**
     * Save the given username as a netbeans.org username.
     * Shouldn't be called in awt
     * @param username
     */
    public static void saveNBUsername(String username) {
        if(username == null) {
            return;
        }
        getPreferences().put(NB_BUGZILLA_USERNAME, username);
    }

    /**
     * Saves the given value as a netbeans.org password
     * Shouldn't be called in awt
     * @param password
     */
    public static void saveNBPassword(char[] password) {
        if(password == null) {
            Keyring.delete(NB_BUGZILLA_PASSWORD);
        } else {
            Keyring.save(
                NB_BUGZILLA_PASSWORD,
                password,
                NbBundle.getMessage(
                    NBBugzillaUtils.class,
                    "NBRepositorySupport.password_keyring_description"));       // NOI18N

        }
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.root().node("org/netbeans/modules/bugtracking"); // NOI18N
    }
}
