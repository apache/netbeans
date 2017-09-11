/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.scenebuilder;

import java.io.File;

/**
 *
 * @author Jaroslav Bachorik
 */
public class Home {
    private String path;
    private String launcherPath;
    private String propertiesPath;
    private String version;

    /**
     * 
     * @param path The home directory absolute path
     * @param launcherPath The launcher path relative to the home path
     * @param propertiesPath The properties path relative to the home path
     * @param version The SB version
     */
    public Home(String path, String launcherPath, String propertiesPath, String version) {
        this.path = path;
        this.version = version;
        this.launcherPath = launcherPath;
        this.propertiesPath = propertiesPath;
    }

    /**
     * 
     * @return The absolute SB home path
     */
    public String getPath() {
        return path;
    }

    /**
     * 
     * @return The absolute SB launcher path
     */
    public String getLauncherPath() {
        return getLauncherPath(false);
    }

    public String getLauncherPath(boolean relative) {
        return (relative ? "" : getPath() + File.separator) + launcherPath;
    }
    
    /**
     * 
     * @return The absolute SB properties path
     */
    public String getPropertiesPath() {
        return getPropertiesPath(false);
    }
    
    public String getPropertiesPath(boolean relative) {
        return (relative ? "" : getPath() + File.separator) + propertiesPath;
    }

    /**
     * 
     * @return The SB version
     */
    public String getVersion() {
        return version;
    }

    /**
     * 
     * @return TRUE if the path property represents a valid SB installation folder
     */
    public boolean isValid() {
        if (path == null) {
            return false;
        }
        File f = new File(path);
        if(!f.exists() || !f.isDirectory()) {
            return false;
        }
        f = new File(getLauncherPath());
        return f.exists() && f.isFile();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Home other = (Home) obj;
        if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
            return false;
        }
        if ((this.version == null) ? (other.version != null) : !this.version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.path != null ? this.path.hashCode() : 0);
        hash = 97 * hash + (this.version != null ? this.version.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return path + " (ver." + version + ")";
    }
}
