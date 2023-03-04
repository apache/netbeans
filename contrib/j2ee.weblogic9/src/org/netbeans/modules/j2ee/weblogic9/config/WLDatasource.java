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

package org.netbeans.modules.j2ee.weblogic9.config;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

/**
 *
 * @author Petr Hejl
 */
public class WLDatasource implements Datasource, WLApplicationModule {

    private final String name;

    private final String url;

    private final String jndi;

    private final String user;

    private final String password;

    private final String driver;

    private final File origin;

    private final boolean system;

    public WLDatasource(String name, String url, String jndi, String user,
            String password, String driver, File origin, boolean system) {
        this.name = name;
        this.url = url;
        this.jndi = jndi;
        this.user = user;
        this.password = password;
        this.driver = driver;
        this.origin = origin;
        this.system = system;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getDriverClassName() {
        return driver;
    }

    @Override
    public String getJndiName() {
        return jndi;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getUsername() {
        return user;
    }

    @Override
    public File getOrigin() {
        return origin;
    }

    public boolean isSystem() {
        return system;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WLDatasource other = (WLDatasource) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.url == null) ? (other.url != null) : !this.url.equals(other.url)) {
            return false;
        }
        if ((this.jndi == null) ? (other.jndi != null) : !this.jndi.equals(other.jndi)) {
            return false;
        }
        if ((this.user == null) ? (other.user != null) : !this.user.equals(other.user)) {
            return false;
        }
        if ((this.driver == null) ? (other.driver != null) : !this.driver.equals(other.driver)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 97 * hash + (this.url != null ? this.url.hashCode() : 0);
        hash = 97 * hash + (this.jndi != null ? this.jndi.hashCode() : 0);
        hash = 97 * hash + (this.user != null ? this.user.hashCode() : 0);
        hash = 97 * hash + (this.driver != null ? this.driver.hashCode() : 0);
        return hash;
    }

}
