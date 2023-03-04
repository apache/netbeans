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

package org.netbeans.modules.apisupport.project.ui.wizard;

import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.WizardDescriptor;

/**
 * Model for storing data gained from <em>NetBeans Plug-in Module</em> wizard
 * panels.
 *
 * @author Martin Krauskopf
 */
final class NewModuleProjectData {

    private WizardDescriptor settings;
    private final NewNbModuleWizardIterator.Type wizardType;

    private boolean netBeansOrg;
    private boolean standalone = true; // standalone is default
    private boolean osgi;
    private String projectName;
    private String projectLocation;
    private String projectFolder;
    private String suiteRoot;
    private String codeNameBase;
    private String platformID;
    private String bundle;
    private String projectDisplayName;
    private int moduleCounter;
    private int suiteCounter;
    private int applicationCounter;
    
    /**
     * @param wizardType
     */
    NewModuleProjectData(NewNbModuleWizardIterator.Type wizardType) {
        this.wizardType = wizardType;
    }
    
    void setSettings(WizardDescriptor settings) {
        this.settings = settings;
    }
    
    WizardDescriptor getSettings() {
        assert settings != null;
        return settings;
    }
    
    void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }
    
    void setNetBeansOrg(boolean netBeansOrg) {
        this.netBeansOrg = netBeansOrg;
    }
    
    boolean isNetBeansOrg() {
        return netBeansOrg;
    }
    
    boolean isStandalone() {
        return standalone;
    }
    
    boolean isOSGi() {
        return osgi;
    }

    void setOsgi(boolean osgi) {
        this.osgi = osgi;
    }
    
    boolean isSuiteComponent() {
        return !isNetBeansOrg() && !isStandalone();
    }
    
    String getProjectName() {
        return projectName;
    }
    
    void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    String getProjectLocation() {
        return projectLocation;
    }
    
    void setProjectLocation(String projectLocation) {
        this.projectLocation = projectLocation;
    }
    
    String getProjectFolder() {
        return projectFolder;
    }
    
    void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }
    
    String getSuiteRoot() {
        return suiteRoot;
    }
    
    void setSuiteRoot(String suiteRoot) {
        this.suiteRoot = suiteRoot;
    }
    
    String getCodeNameBase() {
        return codeNameBase;
    }
    
    void setCodeNameBase(String codeNameBase) {
        this.codeNameBase = codeNameBase;
    }
    
    String getPlatformID() {
        return platformID != null ? platformID : /* #174159 */NbPlatform.PLATFORM_ID_DEFAULT;
    }
    
    void setPlatformID(String platformID) {
        this.platformID = platformID;
    }
    
    String getBundle() {
        return bundle;
    }
    
    void setBundle(String bundle) {
        this.bundle = bundle;
    }
    
    String getProjectDisplayName() {
        return projectDisplayName;
    }
    
    void setProjectDisplayName(String projectDisplayName) {
        this.projectDisplayName = projectDisplayName;
    }
    
    int getModuleCounter() {
        return moduleCounter;
    }
    
    void setModuleCounter(int counter) {
        this.moduleCounter = counter;
    }
    
    int getSuiteCounter() {
        return suiteCounter;
    }
    
    void setSuiteCounter(int counter) {
        this.suiteCounter = counter;
    }
    
    int getApplicationCounter() {
        return applicationCounter;
    }
    
    void setApplicationCounter(int counter) {
        this.applicationCounter = counter;
    }
    
    NewNbModuleWizardIterator.Type getWizardType() {
        return wizardType;
    }
    
}
