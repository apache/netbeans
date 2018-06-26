/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee.db;

import java.util.Objects;
import org.netbeans.modules.glassfish.javaee.ApplicationScopedResourcesUtils.JndiNameResolver;
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
