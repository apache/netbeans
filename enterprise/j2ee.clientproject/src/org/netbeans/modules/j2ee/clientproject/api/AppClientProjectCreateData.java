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

package org.netbeans.modules.j2ee.clientproject.api;

import java.io.File;
import org.netbeans.api.j2ee.core.Profile;

/**
 *
 * @author Petr Hejl
 */
public final class AppClientProjectCreateData {

    private File projectDir;

    private String name;

    private String mainClass;

    private Profile javaEEProfile;

    private String serverInstanceID;

    private String librariesDefinition;

    private File[] sourceFolders;

    private File[] testFolders;

    private File confFolder;

    private File libFolder;

    private boolean cdiEnabled;
    
    public AppClientProjectCreateData() {
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

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
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

    public File getConfFolder() {
        return confFolder;
    }

    public void setConfFolder(File confFolder) {
        this.confFolder = confFolder;
    }

    public File getLibFolder() {
        return libFolder;
    }

    public void setLibFolder(File libFolder) {
        this.libFolder = libFolder;
    }

    public File[] getSourceFolders() {
        return sourceFolders;
    }

    public void setSourceFolders(File[] sourceFolders) {
        this.sourceFolders = sourceFolders;
    }

    public File[] getTestFolders() {
        return testFolders;
    }

    public void setTestFolders(File[] testFolders) {
        this.testFolders = testFolders;
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
