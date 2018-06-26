/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
