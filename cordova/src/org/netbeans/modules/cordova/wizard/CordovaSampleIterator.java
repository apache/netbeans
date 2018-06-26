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

package org.netbeans.modules.cordova.wizard;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cordova.CordovaPerformer;
import org.netbeans.modules.cordova.CordovaPlatform;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.project.ConfigUtils;
import static org.netbeans.modules.cordova.wizard.Bundle.*;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.WizardDescriptor.ProgressInstantiatingIterator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author Martin Janicek
 * @author Jan Becicka
 */
public class CordovaSampleIterator implements ProgressInstantiatingIterator<WizardDescriptor> {

    protected transient Panel[] myPanels;
    protected transient int myIndex;
    protected transient WizardDescriptor descriptor;

    @Override
    public Set<?> instantiate() throws IOException {
        assert false;
        return null;
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        descriptor = wizard;
        myPanels = createPanels(wizard);

        String[] steps = createSteps();
        for (int i = 0; i < myPanels.length; i++) {
            Component c = myPanels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }
    
    @NbBundle.Messages({
        "LBL_NameNLocation=Name and Location",
        "LBL_CordovaSetup=Mobile Platforms Setup"})
    protected String[] createSteps() {
        if (CordovaPlatform.getDefault().isReady()) {
            return new String[] {LBL_NameNLocation()};
        } else {
            return new String[] {LBL_CordovaSetup(), LBL_NameNLocation()};
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        myPanels = null;
    }

       @Override
    public Panel<WizardDescriptor> current() {
        return myPanels[myIndex];
    }

    @Override
    public boolean hasNext() {
        return myIndex < myPanels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return myIndex > 0;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        myIndex++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        myIndex--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    protected Panel[] createPanels(WizardDescriptor wizard) {
        if (CordovaPlatform.getDefault().isReady()) {
            return new Panel[] {new SamplePanel(descriptor)};
        } else {
            return new Panel[] {new CordovaSetupPanel(descriptor),new SamplePanel(descriptor)};
        }
    }

    @Override
    public Set<?> instantiate(ProgressHandle handle) throws IOException {
        FileObject targetFolder = Templates.getTargetFolder(descriptor);

        String targetName = Templates.getTargetName(descriptor);
        FileUtil.toFile(targetFolder).mkdirs();
        FileObject projectFolder = targetFolder.createFolder(targetName);

        FileObject template = Templates.getTemplate(descriptor);
        unZipFile(template.getInputStream(), projectFolder);
        final CordovaPlatform cordovaPlatform = CordovaPlatform.getDefault();
                
        ProjectManager.getDefault().clearNonProjectCache();

        Map<String, String> map = new HashMap<String, String>();
        map.put("CordovaMapsSample", targetName);                             // NOI18N
        ConfigUtils.replaceTokens(projectFolder, map , "nbproject/project.xml"); // NOI18N
        
        final Project project = FileOwnerQuery.getOwner(projectFolder);
        CordovaPerformer.createScript(project, "mapplugins.properties", "nbproject/plugins.properties", true);
        CordovaTemplate.CordovaExtender.setPhoneGapBrowser(project);
        CordovaPerformer.getDefault().createPlatforms(project).waitFinished();
        
        return Collections.singleton(projectFolder);
    }

    private void unZipFile(InputStream source, FileObject rootFolder) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(rootFolder, entry.getName());
                    continue;
                }
                FileObject fo = FileUtil.createData(rootFolder, entry.getName());
                FileLock lock = fo.lock();
                try {
                    OutputStream out = fo.getOutputStream(lock);
                    try {
                        FileUtil.copy(str, out);
                    } finally {
                        out.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        } finally {
            source.close();
        }
    }    
}
