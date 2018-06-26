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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.ejbjarproject.api;

import java.io.File;
import org.netbeans.api.j2ee.core.Profile;

/**
 *
 * @author Petr Hejl
 */
public final class EjbJarProjectCreateData {

    private File projectDir;

    private String name;

    private Profile javaEEProfile;

    private String serverInstanceID;

    private String librariesDefinition;

    private File[] sourceFolders;

    private File[] testFolders;

    private File configFilesBase;

    private File libFolder;

    private boolean cdiEnabled;
    
    public EjbJarProjectCreateData() {
    }

    public Profile getJavaEEProfile() {
        return javaEEProfile;
    }

    public void setJavaEEProfile(Profile javaEEProfile) {
        this.javaEEProfile = javaEEProfile;
    }

    public String getLibrariesDefinition() {
        return librariesDefinition;
    }

    public void setLibrariesDefinition(String librariesDefinition) {
        this.librariesDefinition = librariesDefinition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(File projectDir) {
        this.projectDir = projectDir;
    }

    public String getServerInstanceID() {
        return serverInstanceID;
    }

    public void setServerInstanceID(String serverInstanceID) {
        this.serverInstanceID = serverInstanceID;
    }

    public File getConfigFilesBase() {
        return configFilesBase;
    }

    public void setConfigFilesBase(File configFilesBase) {
        this.configFilesBase = configFilesBase;
    }

    public File getLibFolder() {
        return libFolder;
    }

    public void setLibFolder(File libFolder) {
        this.libFolder = libFolder;
    }

    public File[] getSourceFolders() {
        return sourceFolders != null ? sourceFolders.clone() : null;
    }

    public void setSourceFolders(File[] sourceFolders) {
        this.sourceFolders = sourceFolders != null ? sourceFolders.clone() : null;
    }

    public File[] getTestFolders() {
        return testFolders != null ? testFolders.clone() : null;
    }

    public void setTestFolders(File[] testFolders) {
        this.testFolders = testFolders != null ? testFolders.clone() : null;
    }

    public boolean isCDIEnabled() {
        return cdiEnabled;
    }

    public void setCDIEnabled(boolean cdiEnabled) {
        this.cdiEnabled = cdiEnabled;
    }
    
    public boolean skipTests() {
        return testFolders == null || testFolders.length == 0;
    }

}
