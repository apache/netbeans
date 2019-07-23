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

package org.netbeans.modules.payara.jakartaee.db;

import java.util.Objects;
import org.netbeans.modules.payara.jakartaee.ApplicationScopedResourcesUtils.JndiNameResolver;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

/**
 * Data model for a sun datasource (combined jdbc resource and connection pool).
 * 
 * @author Nitya Doraisamy
 */
public class SunDatasource implements Datasource {

    private final String jndiName;
    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName;

    private final JndiNameResolver resolver;

    public SunDatasource(String jndiName, String url, String username,
            String password, String driverClassName) {
        this(jndiName, url, username, password, driverClassName, false, null);
    }

    public SunDatasource(String jndiName, String url, String username,
            String password, String driverClassName, boolean scoped, JndiNameResolver resolver) {
        this.jndiName = jndiName;
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
        this.resolver = resolver;
    }

    @Override
    public String getDisplayName() {
        return getJndiName();
    }

    @Override
    public String getJndiName() {
        if (resolver != null) {
            return resolver.resolveJndiName(jndiName);
        }
        return jndiName;
    }

    @Override
    public String getUrl() {
        return url;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getDriverClassName() {
        return driverClassName;
    }
    
    @Override
    public String toString() {
        return "[ " + jndiName + " : " + url 
                + " : " + username + " : " + password
                + " : " + driverClassName + " ]";
    }
    
    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SunDatasource other = (SunDatasource) obj;
        if (! Objects.equals(this.jndiName, other.jndiName)) {
            return false;
        }
        if (! Objects.equals(this.url, other.url)) {
            return false;
        }
        if (! Objects.equals(this.username, other.username)) {
            return false;
        }
        if (! Objects.equals(this.password, other.password)) {
            return false;
        }
        if (! Objects.equals(this.driverClassName, other.driverClassName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.jndiName != null ? this.jndiName.hashCode() : 0);
        hash = 41 * hash + (this.url != null ? this.url.hashCode() : 0);
        hash = 41 * hash + (this.username != null ? this.username.hashCode() : 0);
        hash = 41 * hash + (this.password != null ? this.password.hashCode() : 0);
        hash = 41 * hash + (this.driverClassName != null ? this.driverClassName.hashCode() : 0);
        return hash;
    }

    public SunDatasource copy(String jndiName) {
        return new SunDatasource(jndiName, this.url, this.username, 
            this.password, this.driverClassName/*, this.resourceDir*/);
    }

}
