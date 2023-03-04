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
/*
 * SunDatasource.java
 *
 * Created on March 18, 2006, 5:56 PM
 *
 */

package org.netbeans.modules.j2ee.sun.api.restricted;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.openide.util.NbBundle;

/**
 *
 * @author Nitya Doraisamy
 */
public class SunDatasource implements Datasource{
    private String jndiName;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private File resourceDir;
  
    private volatile int hash = -1;
    
    /** Creates a new instance of SunDatasource */
    public SunDatasource(String jndiName, String url, String username, String password, String driverClassName) { 
        this.jndiName = jndiName;
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
    }

    public String getJndiName() {
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

    public String getDisplayName() {
        return jndiName;
    }

    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (!(obj instanceof SunDatasource)){
            return false;
        }
        
        SunDatasource ds = (SunDatasource)obj;
        if (jndiName == null && ds.getJndiName() != null || jndiName != null && !jndiName.equals(ds.getJndiName())){
            return false;
        }
        if (url == null && ds.getUrl() != null || url != null && !url.equals(ds.getUrl())){
            return false;
        }
        if (username == null && ds.getUsername() != null || username != null && !username.equals(ds.getUsername())){
            return false;
        }
        if (password == null && ds.getPassword() != null || password != null && !password.equals(ds.getPassword())){
            return false;
        }
        if (driverClassName == null && ds.getDriverClassName() != null || driverClassName != null && !driverClassName.equals(ds.getDriverClassName())){
            return false;
        }
       
        return true;
    }
    
    public String toString() {
        return "[ " + // NOI18N
                NbBundle.getMessage(SunDatasource.class, "LBL_JNDI") + ": '" + jndiName + "', " + // NOI18N
                NbBundle.getMessage(SunDatasource.class, "LBL_URL") + ": '" + url +  "', " + // NOI18N
                NbBundle.getMessage(SunDatasource.class, "LBL_USER") + ": '" +  username +  "', " + // NOI18N
                NbBundle.getMessage(SunDatasource.class, "LBL_PASS") + ": '" + password +  "', " + // NOI18N
                NbBundle.getMessage(SunDatasource.class, "LBL_DRV") + ": '" + driverClassName +  "' ]"; // NOI18N
    }
    
    public int hashCode() {
        if (hash == -1) {
            int result = 17;
            result += 37 * result + (jndiName == null ? 0 : jndiName.hashCode());
            result += 37 * result + (url == null ? 0 : url.hashCode());
            result += 37 * result + (username == null ? 0 : username.hashCode());
            result += 37 * result + (password == null ? 0 : password.hashCode());
            result += 37 * result + (driverClassName == null ? 0 : driverClassName.hashCode());
            hash = result;
        }
        return hash;
    }    

    public File getResourceDir() {
        return resourceDir;
    }

    public void setResourceDir(File resourceDir) {
        this.resourceDir = resourceDir;
    }
}
