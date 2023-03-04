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

package org.netbeans.modules.j2ee.jboss4.config;

import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;


/**
 *
 * @author Libor Kotouc
 */
public final class JBossDatasource implements Datasource {
    
    public static final String PREFIX = "java:/";
    public static final String SHORT_PREFIX = "java:";
    
    private String rawName;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String minPoolSize = "5"; // NOI18N
    private String maxPoolSize = "20"; // NOI18N
    private String idleTimeoutMinutes = "5"; // NOI18N
    private String description;
    
    private volatile int hash = -1;
    
    public JBossDatasource(String jndiName, String url, String username, String password,
            String driverClassName) {
        this.rawName = jndiName;
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
    }

    public String getJndiName() {
        return getJndiName(rawName);
    }

    /**
     * Returns JNDI name in the correct run-time format, i.e. "java:/..."
     */
    public static String getJndiName(String rawName) {
        
        Parameters.notNull("rawName", rawName);
        
        if (rawName.startsWith(PREFIX)) {
            return rawName;
        }
        
        if (rawName.startsWith(SHORT_PREFIX)) {
            return PREFIX + rawName.substring(5); // SHORT_PREFIX.length() == 5
        }
        
        if (rawName.startsWith("/")) {
            return SHORT_PREFIX + rawName;
        }
        
        // TODO check other formats
        
        return PREFIX + rawName;
    }
    
    /**
     * Returns DS name in the 'resource-file' format
     */
    public static String getRawName(String jndiName) {

        Parameters.notNull("jndiName", jndiName);
        
        if (jndiName.startsWith(PREFIX)) {
            return jndiName.substring(PREFIX.length());
        }
        else
        if (jndiName.startsWith(SHORT_PREFIX)) {
            return jndiName.substring(SHORT_PREFIX.length());
        }
        else
        if (jndiName.startsWith("/")) {
            return jndiName.substring(1);
        }
        
        // TODO check other formats

        return jndiName;
    }
    
    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getMinPoolSize() {
        return minPoolSize;
    }

    public String getMaxPoolSize() {
        return maxPoolSize;
    }

    public String getIdleTimeoutMinutes() {
        return idleTimeoutMinutes;
    }
    
    public String getDisplayName() {
        if (description == null) {
            //TODO implement some meaningful description
            description = getJndiName() + " [" + getUrl() + "]";
        }
        return description;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof JBossDatasource))
            return false;
        
        JBossDatasource ds = (JBossDatasource)obj;
        if (getJndiName() == null && ds.getJndiName() != null || getJndiName() != null && !getJndiName().equals(ds.getJndiName()))
            return false;
        if (url == null && ds.getUrl() != null || url != null && !url.equals(ds.getUrl()))
            return false;
        if (username == null && ds.getUsername() != null || username != null && !username.equals(ds.getUsername()))
            return false;
        if (password == null && ds.getPassword() != null || password != null && !password.equals(ds.getPassword()))
            return false;
        if (driverClassName == null && ds.getDriverClassName() != null || driverClassName != null && !driverClassName.equals(ds.getDriverClassName()))
            return false;
        if (minPoolSize == null && ds.getMinPoolSize() != null ||  minPoolSize != null && !minPoolSize.equals(ds.getMinPoolSize()))
            return false;
        if (maxPoolSize == null && ds.getMaxPoolSize() != null || maxPoolSize != null && !maxPoolSize.equals(ds.getMaxPoolSize()))
            return false;
        if (idleTimeoutMinutes == null && ds.getIdleTimeoutMinutes() != null || idleTimeoutMinutes != null && !idleTimeoutMinutes.equals(ds.getIdleTimeoutMinutes()))
            return false;
        
        return true;
    }
    
    public int hashCode() {
        if (hash == -1) {
            int result = 17;
            result += 37 * result + (getJndiName() == null ? 0 : getJndiName().hashCode());
            result += 37 * result + (url == null ? 0 : url.hashCode());
            result += 37 * result + (username == null ? 0 : username.hashCode());
            result += 37 * result + (password == null ? 0 : password.hashCode());
            result += 37 * result + (driverClassName == null ? 0 : driverClassName.hashCode());
            result += 37 * result + (minPoolSize == null ? 0 : minPoolSize.hashCode());
            result += 37 * result + (maxPoolSize == null ? 0 : maxPoolSize.hashCode());
            result += 37 * result + (idleTimeoutMinutes == null ? 0 : idleTimeoutMinutes.hashCode());
            
            hash = result;
        }
        
        return hash;
    }
    
    public String toString() {
        return "[ " + // NOI18N
                NbBundle.getMessage(JBossDatasource.class, "LBL_DS_JNDI") + ": '" + getJndiName() + "', " + // NOI18N
                NbBundle.getMessage(JBossDatasource.class, "LBL_DS_URL") + ": '" + url +  "', " + // NOI18N
                NbBundle.getMessage(JBossDatasource.class, "LBL_DS_USER") + ": '" +  username +  "', " + // NOI18N
                NbBundle.getMessage(JBossDatasource.class, "LBL_DS_PASS") + ": '" + password +  "', " + // NOI18N
                NbBundle.getMessage(JBossDatasource.class, "LBL_DS_DRV") + ": '" + driverClassName +  "', " + // NOI18N
                NbBundle.getMessage(JBossDatasource.class, "LBL_DS_MINPS") + ": '" + minPoolSize +  "', " + // NOI18N
                NbBundle.getMessage(JBossDatasource.class, "LBL_DS_MAXPS") + ": '" + maxPoolSize +  "', " + // NOI18N
                NbBundle.getMessage(JBossDatasource.class, "LBL_DS_IDLE") + ": '" + idleTimeoutMinutes +  "' ]"; // NOI18N
    }
}
