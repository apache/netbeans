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

package org.netbeans.modules.tomcat5.config;

import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

import java.util.Objects;

/**
 * Tomcat datasource implementation
 *
 * @author sherold
 */
public class TomcatDatasource implements Datasource {
    
    private final String username;
    private final String url;
    private final String password;
    private final String jndiName;
    private final String driverClassName;
    private int hash;
    
    /**
     * Creates a new instance of TomcatDatasource
     */
    public TomcatDatasource(String username, String url, String password, String jndiName, String driverClassName) {
        this.username = username;
        this.url = url;
        this.password = password;
        this.jndiName = jndiName;
        this.driverClassName = driverClassName;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getJndiName() {
        return jndiName;
    }

    @Override
    public String getDriverClassName() {
        return driverClassName;
    }

    @Override
    public String getDisplayName() {
        return jndiName + " [" + url + "]"; // NOI18N
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TomcatDatasource)) {
            return false;
        }
        TomcatDatasource ds = (TomcatDatasource)obj;
        if (!Objects.equals(jndiName, ds.jndiName)) {
            return false;
        }
        if (!Objects.equals(url, ds.url)) {
            return false;
        }
        if (!Objects.equals(username, ds.username)) {
            return false;
        }
        if (!Objects.equals(password, ds.password)) {
            return false;
        }
        if (!Objects.equals(driverClassName, ds.driverClassName)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        if (hash == 0) {
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TomcatDatasource [username=").append(username); // NOI18N
        sb.append(", url=").append(url); // NOI18N
        sb.append(", password=").append(password); // NOI18N
        sb.append(", jndiName=").append(jndiName); // NOI18N
        sb.append(", driverClassName=").append(driverClassName).append("]"); // NOI18N
        return sb.toString();
    }
    
}
