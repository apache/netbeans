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

package org.netbeans.modules.groovy.grailsproject.plugins;

import java.io.File;

/**
 * plugin pojo class
 *
 * @author David Calavera
 */
public class GrailsPlugin implements Comparable<GrailsPlugin> {
    private final String name;
    private final String version;
    private final String description;
    private final File path;
    private final String dirName;
    private final String zipName;

    public GrailsPlugin(String name, String version, String description) {
        this(name, version, description, null);
    }

    // FIXME null values
    public GrailsPlugin(String name, String version, String description, File path) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.path = path;
        this.dirName = name + "-" + version;
        this.zipName = "grails-" + dirName + ".zip";
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public File getPath() {
        return path;
    }

    public String getDirName() {
        return dirName;
    }

    public String getZipName() {
        return zipName;
    }

    @Override
    public String toString() {
        String toS = getName();
        if (getVersion() != null && getVersion().trim().length() > 0) {
            toS += "(" + getVersion().trim() + ")";
        }
        if (getDescription() != null && getDescription().trim().length() > 0
                && !getDescription().trim().equals("No description available")) {
            toS += " -- " + getDescription().trim();
        }
        return toS;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GrailsPlugin other = (GrailsPlugin) obj;
        if (this.name != other.name && (this.name == null || !this.name.equalsIgnoreCase(other.name))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.name != null ? this.name.toUpperCase().hashCode() : 0);
        return hash;
    }

    public int compareTo(GrailsPlugin o) {
        return name.compareToIgnoreCase(o.name);
    }

}
