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
