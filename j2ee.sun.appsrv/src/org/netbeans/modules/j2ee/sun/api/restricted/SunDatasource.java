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
