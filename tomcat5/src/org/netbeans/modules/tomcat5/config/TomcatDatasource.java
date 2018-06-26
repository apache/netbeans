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

package org.netbeans.modules.tomcat5.config;

import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

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

    public String getUsername() {
        return username;
    }

    public String getUrl() {
        return url;
    }

    public String getPassword() {
        return password;
    }

    public String getJndiName() {
        return jndiName;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getDisplayName() {
        return jndiName + " [" + url + "]"; // NOI18N
    }
    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TomcatDatasource)) {
            return false;
        }
        TomcatDatasource ds = (TomcatDatasource)obj;
        if ((jndiName == null && ds.jndiName != null) || (jndiName != null && !jndiName.equals(ds.jndiName))) {
            return false;
        }
        if ((url == null && ds.url != null) || (url != null && !url.equals(ds.url))) {
            return false;
        }
        if ((username == null && ds.username != null) || (username != null && !username.equals(ds.username))) {
            return false;
        }
        if ((password == null && ds.password != null) || (password != null && !password.equals(ds.password))) {
            return false;
        }
        if ((driverClassName == null && ds.driverClassName != null) || (driverClassName != null && !driverClassName.equals(ds.driverClassName))) {
            return false;
        }
        return true;
    }
    
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
