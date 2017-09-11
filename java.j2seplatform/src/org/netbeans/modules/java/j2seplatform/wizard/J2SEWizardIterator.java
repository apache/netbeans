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
