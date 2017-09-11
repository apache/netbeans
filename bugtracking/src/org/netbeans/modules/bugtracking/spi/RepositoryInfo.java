/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.spi;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.commons.NBBugzillaUtils;
import org.netbeans.modules.team.commons.LogUtils;

/**
 * Represents information related to one particular repository. 
 *
 * @author Tomas Stupka
 * @see RepositoryController
 * @since 1.85
 */
public final class RepositoryInfo {
    
    private static final Logger LOG = Logger.getLogger(RepositoryInfo.class.getName());
    
    static {
        SPIAccessorImpl.createAccesor();
    }

    private static final String DELIMITER         = "<=>";                      // NOI18N    
    
    private final Map<String, String> map = new HashMap<String, String>();
    
    private static final String PROPERTY_ID = "id";                             // NOI18N    
    private static final String PROPERTY_CONNECTOR_ID = "connectorId";          // NOI18N    
    private static final String PROPERTY_URL = "url";                           // NOI18N    
    private static final String PROPERTY_DISPLAY_NAME = "displayName";          // NOI18N    
    private static final String PROPERTY_TOOLTIP = "tooltip";                   // NOI18N    
    private static final String PROPERTY_USERNAME = "username";                 // NOI18N    
    private static final String PROPERTY_HTTP_USERNAME = "httpUsername";        // NOI18N    

    private RepositoryInfo(Map<String, String> properties) {
        logMap("create RepositoryInfo for ",properties);
        this.map.putAll(properties);
    }

    /**
     * Creates a new RepositoryInfo instance.
     * 
     * @param id unique identifier for the given connector
     * @param connectorId connector id
     * @param url remote repository url
     * @param displayName display name to be used in the UI
     * @param tooltip tooltip to be used in the UI
     * @since 1.85
     */
    public RepositoryInfo(String id, String connectorId, String url, String displayName, String tooltip) {
        LOG.log(
            Level.FINER, 
                "create RepositoryInfo for [id={0},connectorId={1},url={2},displayName={3},tooltip={4}]", 
                new Object[]{id, connectorId, url, displayName, tooltip});
        
        map.put(PROPERTY_ID, id);
        map.put(PROPERTY_CONNECTOR_ID, connectorId);
        map.put(PROPERTY_DISPLAY_NAME, displayName);
        map.put(PROPERTY_TOOLTIP, tooltip);
        map.put(PROPERTY_URL, url);
    }
    
    /**
     * Creates a new RepositoryInfo instance.
     * 
     * @param id unique identifier for the given connector
     * @param connectorId connector id
     * @param url remote repository url
     * @param displayName display name to be used in the UI
     * @param tooltip tooltip to be used in the UI
     * @param user username 
     * @param httpUser http username 
     * @param password password
     * @param httpPassword http password
     * @since 1.85
     */
    public RepositoryInfo(String id, String connectorId, String url, String displayName, String tooltip, String user, String httpUser, char[] password, char[] httpPassword) {
        LOG.log(
            Level.FINER, 
                "create RepositoryInfo for [id={0},connectorId={1},url={2},displayName={3},tooltip={4},user={5},httpUser={6},password={7},httpPassword={8}]", 
                new Object[]{id, connectorId, url, displayName, tooltip, user, httpUser, LogUtils.getPasswordLog(password), LogUtils.getPasswordLog(httpPassword)});
        
        map.put(PROPERTY_ID, id);
        map.put(PROPERTY_CONNECTOR_ID, connectorId);
        map.put(PROPERTY_DISPLAY_NAME, displayName);
        map.put(PROPERTY_TOOLTIP, tooltip);
        map.put(PROPERTY_URL, url);
        map.put(PROPERTY_USERNAME, user);
        map.put(PROPERTY_HTTP_USERNAME, httpUser);
        storePasswords(password, httpPassword);
    }

    /**
     * Returns the display name to presented in the IDE UI.
     * 
     * @return display name
     * @since 1.85
     */
    public String getDisplayName() {
        return map.get(PROPERTY_DISPLAY_NAME);
    }

    /**
     * Returns the http password.
     * 
     * @return http password
     * @since 1.85
     */
    public char[] getHttpPassword() {
        if(isNbRepository(map)) {
            return new char[0];
        } else {
            char[] httpPassword = BugtrackingUtil.readPassword(null, "http", getHttpUsername(), getUrl());
            LOG.log(Level.FINER, "read httpPassword={0}", LogUtils.getPasswordLog(httpPassword));
            return httpPassword; // NOI18N
        }
    }

    /**
     * Returns this repository's unique ID.
     * 
     * @return id
     * @since 1.85
     */
    public String getID() {
        return map.get(PROPERTY_ID);
    }
    
    /**
     * Returns the id for the connector this repository belongs to.
     * 
     * @return connector id
     * @since 1.85
     */
    public String getConnectorId() {
        return map.get(PROPERTY_CONNECTOR_ID);
    }
    
    /**
     * Returns the remote url of this repository.
     * 
     * @return url
     * @since 1.85
     */
    public String getUrl() {
        return map.get(PROPERTY_URL);
    }

    /**
     * Returns the password for the repository user.
     * 
     * @return password
     * @since 1.85
     */
    public char[] getPassword() {
        if(isNbRepository(map)) {
            char[] password = NBBugzillaUtils.getNBPassword();
            LOG.log(Level.FINER, "read netbeans password={0}", LogUtils.getPasswordLog(password));
            return password;
        } else {
            char[] password = BugtrackingUtil.readPassword(null, null, getUsername(), getUrl());
            LOG.log(Level.FINER, "read password={0}", LogUtils.getPasswordLog(password));
            return password;
        }
    }

    /**
     * Returns the tooltip to be presented in the IDE UI.
     * 
     * @return tooltip
     * @since 1.85
     */
    public String getTooltip() {
        return map.get(PROPERTY_TOOLTIP);
    }

    /**
     * Returns the username of this repository.
     * 
     * @return username
     * @since 1.85
     */
    public String getUsername() {
        return map.get(PROPERTY_USERNAME);
    }

    /**
     * Returns the http username.
     * 
     * @return the http username
     * @since 1.85
     */
    public String getHttpUsername() {
        return map.get(PROPERTY_HTTP_USERNAME);
    }
    
    /**
     * Gets a general property of a Repository. 
     * 
     * @param key property key
     * @return property value
     * @since 1.85
     */
    public String getValue(String key) {
        return map.get(key);
    }
    
    /**
     * Sets a general property of a Repository. 
     * 
     * @param key property key
     * @param value property value
     * @since 1.85
     */
    public void putValue(String key, String value) {
        map.put(key, value);
    }

    static RepositoryInfo read(Preferences preferences, String key) {
        String str = preferences.get(key, "");                                  // NOI18N    
        if(str.equals("")) {                                                    // NOI18N    
            return null;
        }
        Map<String, String> m = fromString(str);
        if(isNbRepository(m)) {
            m.put(PROPERTY_USERNAME, NBBugzillaUtils.getNBUsername());
        }
        return new RepositoryInfo(m);
    }
    
    void store(Preferences preferences, String key) {
        boolean isNetbeans = isNbRepository(map);
        preferences.put(key, getStringValue(isNetbeans));
        if(isNetbeans) {
            NBBugzillaUtils.saveNBUsername(getUsername());
        }
    }
    
    private String getStringValue(boolean dropUser) {
        List<String> l = new ArrayList<String>(map.keySet());
        Collections.sort(l);
        StringBuilder sb = new StringBuilder();
        sb.append(PROPERTY_ID);
        sb.append(DELIMITER);
        sb.append(map.get(PROPERTY_ID));
        for (String key : l) {
            if(!key.equals(PROPERTY_ID)) {
                sb.append(DELIMITER);
                sb.append(key);
                sb.append(DELIMITER);
                if(!(dropUser && key.equals(PROPERTY_USERNAME))) {
                    String value = map.get(key);
                    if(value != null) {
                        sb.append(value);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static Map<String, String> fromString(String string) {
        String[] values = string.split(DELIMITER);
        Map<String, String> m = new HashMap<String, String>(); 
        for (int i = 0; i < values.length; i = i + 2) {
            String key = values[i];
            String value = i < values.length - 1 ? values[i + 1] : "";
            m.put(key, value);
        }
        return m;
    }   
    
    private void storePasswords(char[] password, char[] httpPassword) throws MissingResourceException {
        if(isNbRepository(map)) {
            LOG.log(Level.FINER, "storing netbeans password={0}", LogUtils.getPasswordLog(password));
            NBBugzillaUtils.saveNBPassword(password);
        } else {
            LOG.log(Level.FINER, "storing password={0}, httpPassword={1}", new Object[] {LogUtils.getPasswordLog(password), LogUtils.getPasswordLog(httpPassword)});
            BugtrackingUtil.savePassword(password, null, getUsername(), getUrl());
            BugtrackingUtil.savePassword(httpPassword, "http", getHttpUsername(), getUrl()); // NOI18N
        }
    }

    private static boolean isNbRepository(Map<String, String> map) {
        String url = map.get(PROPERTY_URL);
        return url != null ? NBBugzillaUtils.isNbRepository(url) : false;
    }

    private void logMap(String prefix, Map<String, String> properties) {
        if(!LOG.isLoggable(Level.FINER)) {
            return;
        }
        HashMap<String, String> m = new HashMap<String, String>(properties);
        StringBuilder sb = new StringBuilder();
        if(prefix != null) {
            sb.append(prefix);
        }
        sb.append("[");
        sb.append(PROPERTY_ID);
        sb.append("=");
        sb.append(m.get(PROPERTY_ID));
        sb.append(",");
        m.remove(PROPERTY_ID);
        String[] keys = m.keySet().toArray(new String[m.size()]);
        Arrays.sort(keys);
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            sb.append(key);
            sb.append("=");
            sb.append(m.get(key));
            if(i < keys.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        LOG.finer(sb.toString());
    }
}
