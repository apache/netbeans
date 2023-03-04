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

package org.netbeans.modules.java.j2seplatform;

import org.openide.filesystems.*;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.netbeans.modules.java.j2seplatform.wizard.J2SEWizardIterator;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;

import java.io.IOException;
import java.util.Collections;

import org.openide.WizardDescriptor;

/**
 * Installer factory for standard J2SE Platforms
 *
 * @author Svatopluk Dedic
 */
class J2SEInstallImpl extends org.netbeans.spi.java.platform.PlatformInstall {

    J2SEInstallImpl() {
    }
    
    static J2SEInstallImpl create() {        
        return new J2SEInstallImpl();
    }
    
    /**
     * Performs a quick & dirty check whether there's a JRE installed.
     * The method looks into the folder for something, which - depending on 
     * the platform's conventions - has name "java.exe" or "java"
     */
    @Override
    public boolean accept(FileObject dir) {
        if (!dir.isFolder()) {
            return false;
        }
        FileObject tool = Util.findTool("java", Collections.singleton(dir));    //NOI18N
        if (tool == null) {
            return false;
        }
        tool = Util.findTool("javac", Collections.singleton(dir));  //NOI18N
        return tool != null;
    }
    
    @Override
    public WizardDescriptor.InstantiatingIterator<WizardDescriptor> createIterator(FileObject baseFolder) {
        try {
            return new J2SEWizardIterator(baseFolder);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify (ioe);
            return null;
        }
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(J2SEInstallImpl.class,"TXT_J2SEPlatform");
    }
}
