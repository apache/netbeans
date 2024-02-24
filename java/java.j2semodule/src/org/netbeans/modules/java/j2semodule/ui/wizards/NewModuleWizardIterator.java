/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.j2semodule.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.java.api.common.util.CommonModuleUtils;
import org.netbeans.modules.java.j2semodule.J2SEModularProject;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class NewModuleWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {

    private final transient Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);    
    private transient WizardDescriptor.Panel panel;
    private transient WizardDescriptor wiz;

    @TemplateRegistration(
            folder = "J2SEModule",
            position = 1,
            content = "../resources/module-info.java.template",
            scriptEngine = "freemarker",
            displayName = "#moduleWizard",
            iconBase = "org/netbeans/modules/java/j2semodule/ui/resources/module.png",
            description = "../resources/module.html",
            category = {"java-modules"})
    @NbBundle.Messages("moduleWizard=Module")
    public static NewModuleWizardIterator moduleWizard() {
        return new NewModuleWizardIterator();
    }

    @Override
    public Set instantiate() throws IOException {
        FileObject dir = Templates.getTargetFolder(wiz);        
        String targetName = Templates.getTargetName(wiz);
        FileObject template = Templates.getTemplate(wiz);

        final List<FileObject> createdFolders = new ArrayList<>();
        final FileObject moduleFolder = FileUtil.createFolder(dir, targetName);
        createdFolders.add(moduleFolder);

        Project p = Templates.getProject(wiz);
        J2SEModularProject project = p != null ? p.getLookup().lookup(J2SEModularProject.class) : null;
        if (project != null) {
            String[] rootProperties = project.getModuleRoots().getRootProperties();
            String[] rootPathProperties = project.getModuleRoots().getRootPathProperties();
            assert rootProperties.length == rootPathProperties.length;
            for (int i = 0; i < rootProperties.length; i++) {
                String rootProp = project.evaluator().getProperty(rootProperties[i]);
                if (rootProp != null && dir == project.getAntProjectHelper().resolveFileObject(rootProp)) {
                    final Collection<? extends String> spVariants =
                            Arrays.stream(PropertyUtils.tokenizePath(project.evaluator().getProperty(rootPathProperties[i])))
                            .map((pe) -> CommonModuleUtils.parseSourcePathVariants(pe))
                            .flatMap((lv) -> lv.stream())
                            .collect(Collectors.toList());
                    for (String variant : spVariants) {
                        if (!variant.isEmpty()) {
                            createdFolders.add(FileUtil.createFolder(moduleFolder, variant.replace(File.separatorChar, '/')));
                        }                        
                    }
                }
            }
        }

        final DataFolder df = DataFolder.findFolder(createdFolders.get(
                createdFolders.size() > 1 ? 1 : 0));
        DataObject dTemplate = DataObject.find(template);
        DataObject dobj = dTemplate.createFromTemplate(df, null, Collections.singletonMap("moduleName", targetName)); //NOI18N
        FileObject createdFile = dobj.getPrimaryFile();

        final Set<FileObject> res = new HashSet<>();
        res.addAll(createdFolders);
        res.add(createdFile);
        return Collections.unmodifiableSet(res);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wiz = wizard;
        // Ask for Java folders
        Project project = Templates.getProject(wiz);
        if (project == null) {
            throw new NullPointerException ("No project found for: " + wiz);
        }
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_MODULES);
        assert groups != null : "Cannot return null from Sources.getSourceGroups: " + sources;
        if (groups.length == 0) {
            groups = sources.getSourceGroups(Sources.TYPE_GENERIC); 
        }
        panel = new ModuleTargetChooserPanel(project, groups);
        // Make sure list of steps is accurate.
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
        String[] beforeSteps = prop instanceof String[] ? (String[])prop : new String[0];
        int diff = 0;
        if (beforeSteps.length > 0) {
            diff = ("...".equals (beforeSteps[beforeSteps.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] steps = new String[ (beforeSteps.length - diff) + 1];
        for (int i = 0; i < steps.length; i++) {
            if (i < (beforeSteps.length - diff)) {
                steps[i] = beforeSteps[i];
            } else {
                steps[i] = panel.getComponent ().getName ();
            }
        }
        Component c = panel.getComponent();
        if (c instanceof JComponent) { // assume Swing components
            JComponent jc = (JComponent)c;
            // Step #.
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(0));
            // Step name (actually the whole list for reference).
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        this.wiz = null;
        panel = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panel;
    }

    @Override
    public String name() {
        return ""; //NOI18N
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
    public void nextPanel() {
        throw new NoSuchElementException();
    }

    @Override
    public void previousPanel() {
        throw new NoSuchElementException();
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        ChangeListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ChangeListener[0]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }
}
