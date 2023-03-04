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

package org.netbeans.modules.java.j2seplatform.wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.platformdefinition.PlatformConvertor;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Wizard Iterator for standard J2SE platforms. It assumes that there is a
 * 'bin{/}java[.exe]' underneath the platform's directory, which can be run to
 * produce the target platform's VM environment.
 *
 * @author Svata Dedic, Tomas Zezula
 */
public class J2SEWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {
    
    private static final String[] SOLARIS_64_FOLDERS = {"sparcv9","amd64"};     //NOI18N

    DataFolder                  installFolder;
    DetectPanel.WizardPanel     detectPanel;
    final ChangeSupport  listeners = new ChangeSupport(this);
    NewJ2SEPlatform             platform;
    NewJ2SEPlatform             secondaryPlatform;
    WizardDescriptor            wizard;
    int                         currentIndex;

    public J2SEWizardIterator(FileObject installFolder) throws IOException {
        this.installFolder = DataFolder.findFolder(installFolder);
        this.platform = NewJ2SEPlatform.create (installFolder);        
        String archFolder = null;
        for (int i = 0; i< SOLARIS_64_FOLDERS.length; i++) {
            if (Util.findTool("java",Collections.singleton(installFolder),SOLARIS_64_FOLDERS[i]) != null) {
                archFolder = SOLARIS_64_FOLDERS[i];
                break;
            }
        }
        if (archFolder != null) {
            this.secondaryPlatform  = NewJ2SEPlatform.create (installFolder);
            this.secondaryPlatform.setArchFolder(archFolder);
        }
    }

    FileObject getInstallFolder() {
        return installFolder.getPrimaryFile();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.addChangeListener(l);
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        switch (this.currentIndex) {
            case 0:
                return this.detectPanel;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wizard = wiz;
        this. detectPanel = new DetectPanel.WizardPanel(this);
        this.currentIndex = 0;
    }

    /**
     * This finally produces the java platform's XML that represents the basic
     * platform's properties. The XML is returned in the resulting Set.
     * @return singleton Set with java platform's instance DO inside.
     */
    @Override
    public java.util.Set instantiate() throws IOException {
        //Workaround #44444
        this.detectPanel.storeSettings (this.wizard);
        Set<JavaPlatform> result = new HashSet<JavaPlatform> ();
        for (NewJ2SEPlatform platform : getPlatforms()) {
            if (platform.isValid()) {
                try {
                    result.add(PlatformConvertor.create(platform));
                    if (result.size() == 1) {
                        getInstallFolder().setAttribute(
                                NewJ2SEPlatform.DISPLAY_NAME_FILE_ATTR,
                                platform.getDisplayName());
                    }
                } catch (IllegalArgumentException iae) {
                    throw new IllegalStateException(NbBundle.getMessage(J2SEWizardIterator.class,"ERROR_InvalidName"));
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public String name() {
        return NbBundle.getMessage(J2SEWizardIterator.class, "TITLE_PlatformName");
    }

    @Override
    public void nextPanel() {
        this.currentIndex++;
    }

    @Override
    public void previousPanel() {
        this.currentIndex--;
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.removeChangeListener(l);
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wizard = null;        
        this.detectPanel = null;
    }

    public NewJ2SEPlatform getPlatform() {
        return this.platform;
    }      
    
    public NewJ2SEPlatform getSecondaryPlatform () {
        return this.secondaryPlatform;
    }
    
    private List<NewJ2SEPlatform> getPlatforms () {
        List<NewJ2SEPlatform> result = new ArrayList<NewJ2SEPlatform> ();
        result.add(this.platform);
        if (this.secondaryPlatform != null) {
            result.add(this.secondaryPlatform);
        }
        return result;
    }
}
