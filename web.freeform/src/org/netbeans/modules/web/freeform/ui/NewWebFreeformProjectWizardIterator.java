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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.web.freeform.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.ant.freeform.spi.TargetDescriptor;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.spi.support.NewJavaFreeformProjectSupport;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.freeform.WebProjectGenerator;
import org.netbeans.modules.web.freeform.WebProjectNature;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny, Radko Najman
 */
public class NewWebFreeformProjectWizardIterator implements WizardDescriptor.InstantiatingIterator {

    // web sources
    public static final String PROP_WEB_WEBMODULES = "webModules"; // <List> NOI18N
    public static final String PROP_WEB_SOURCE_FOLDERS = "webSourceFolders"; // <List> NOI18N
    public static final String PROP_WEB_INF_FOLDER = "webInfFolder"; // <List> NOI18N
    
    protected static final String PROP_WEB_CLASSPATH = "webClasspath"; // <String> NOI18N
    
    private static final long serialVersionUID = 1L;
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public NewWebFreeformProjectWizardIterator() {
    }
    
    private WizardDescriptor.Panel[] createPanels () {
        List<WizardDescriptor.Panel> l = new ArrayList<WizardDescriptor.Panel>();
        List<TargetDescriptor> extraTargets = new ArrayList<TargetDescriptor>();
        extraTargets.add(WebProjectNature.getExtraTarget());
        l.add(NewFreeformProjectSupport.createBasicProjectInfoWizardPanel());
        l.add(NewFreeformProjectSupport.createTargetMappingWizardPanel(extraTargets));
        l.add(new WebLocationsWizardPanel());
        l.addAll(Arrays.asList(NewJavaFreeformProjectSupport.createJavaPanels()));
        l.add(new WebClasspathWizardPanel());
        return l.toArray(new WizardDescriptor.Panel[l.size()]);
    }
    
    public Set<FileObject> instantiate () throws IOException {
        final WizardDescriptor wiz = this.wiz;
        final IOException[] ioe = new IOException[1];
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    AntProjectHelper helper = NewFreeformProjectSupport.instantiateBasicProjectInfoWizardPanel(wiz);
                    NewFreeformProjectSupport.instantiateTargetMappingWizardPanel(helper, wiz);
                    NewJavaFreeformProjectSupport.instantiateJavaPanels(helper, wiz);
                    
                    @SuppressWarnings("unchecked")
                    List<String> webSources = (List<String>)wiz.getProperty(PROP_WEB_SOURCE_FOLDERS);
                    @SuppressWarnings("unchecked")
                    List<String> webInf = (List<String>)wiz.getProperty(PROP_WEB_INF_FOLDER);
                    AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(helper);
                    WebProjectGenerator.putWebSourceFolder(helper, webSources);
                    WebProjectGenerator.putWebInfFolder(helper, webInf);
        
                    @SuppressWarnings("unchecked")
                    List<WebProjectGenerator.WebModule> webModules = (List<WebProjectGenerator.WebModule>) wiz.getProperty(PROP_WEB_WEBMODULES);
                    if (webModules != null) {
                        // Save the web classpath for the web module
                        String webClasspath = (String)wiz.getProperty(NewWebFreeformProjectWizardIterator.PROP_WEB_CLASSPATH);
                        Iterator iter = webModules.iterator();
                        for (WebProjectGenerator.WebModule wm : webModules) {
                            wm.classpath = webClasspath;
                        }
                        WebProjectGenerator.putWebModules (helper, aux, webModules);
                    }
                    
                    Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
                    ProjectManager.getDefault().saveProject(p);
                } catch (IOException e) {
                    ioe[0] = e;
                    return;
                }
            }});
        if (ioe[0] != null) {
            throw ioe[0];
        }
        File nbProjectFolder = (File)wiz.getProperty(NewFreeformProjectSupport.PROP_PROJECT_FOLDER);
        Set<FileObject> resultSet = new HashSet<FileObject>();
        resultSet.add(FileUtil.toFileObject(nbProjectFolder));
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(nbProjectFolder));
        if (p != null) {
            Sources srcs = ProjectUtils.getSources(p);
            if (srcs != null) {
                SourceGroup[] grps = srcs.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
                if (grps != null && grps.length > 0) {
                    resultSet.add(grps[0].getRootFolder());
                }
            }
        }
        File f = nbProjectFolder.getParentFile();
        if (f != null) {
            ProjectChooser.setProjectsFolder(f);
        }
        return resultSet;
    }
    
        
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        
        List<String> l = new ArrayList<String>();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert c instanceof JComponent;
            JComponent jc = (JComponent)c;
            l.add(jc.getName());
        }
        String[] steps = l.toArray(new String[l.size()]);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert c instanceof JComponent;
            JComponent jc = (JComponent)c;
            // Step #.
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
            // Step name (actually the whole list for reference).
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            // set title
            jc.putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage (NewWebFreeformProjectWizardIterator.class, "TXT_NewWebFreeformProjectWizardIterator_NewProjectWizardTitle")); // NOI18N
            jc.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (NewWebFreeformProjectWizardIterator.class, "ACSD_NewWebFreeformProjectWizardIterator_NewProjectWizardTitle")); // NOI18N
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        NewFreeformProjectSupport.uninitializeBasicProjectInfoWizardPanel(wiz);
        NewFreeformProjectSupport.uninitializeTargetMappingWizardPanel(wiz);
        NewJavaFreeformProjectSupport.uninitializeJavaPanels(wiz);
        wiz.putProperty(PROP_WEB_SOURCE_FOLDERS, null);
        wiz.putProperty(PROP_WEB_INF_FOLDER, null);
        wiz.putProperty(PROP_WEB_WEBMODULES, null);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format (NbBundle.getMessage(NewWebFreeformProjectWizardIterator.class, "TXT_NewWebFreeformProjectWizardIterator_TitleFormat"), // NOI18N
            new Object[] {new Integer (index + 1), new Integer (panels.length) });
    }
    
    public boolean hasNext() {
        if (!NewJavaFreeformProjectSupport.enableNextButton(current())) {
            return false;
        }
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}

    
}
