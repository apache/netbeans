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

package org.netbeans.modules.maven.newproject.idenative;

import org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder;
import java.io.File;
import org.netbeans.modules.maven.newproject.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.codehaus.plexus.util.StringUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.api.validation.adapters.WizardDescriptorAdapter;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import static org.netbeans.modules.maven.newproject.idenative.Bundle.LBL_CreateProjectStep2;
import static org.netbeans.modules.maven.newproject.idenative.Bundle.NameFormat;

import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

/**
 *
 *@author mkleint
 */
public abstract class IDENativeMavenWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor>, WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {
    private static final long serialVersionUID = 1L;
    
    private transient int index;
    private transient List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    private transient WizardDescriptor wiz;

    private final AtomicBoolean hasNextCalled = new AtomicBoolean(); //#216236
    private final String titlename;
    private final String log;
    private final String packaging;

    public IDENativeMavenWizardIterator(String title, String log, String packaging) {
        this.titlename = title;
        this.log = log;
        this.packaging = packaging;
    }
    
    @Override
    public Set instantiate() throws IOException {
        throw new UnsupportedOperationException("Not supported."); 
    }
    
    @Override
    @Messages({"PRG_Dir=Creating directory", "PRG_FINISH=Finishing..."})
    public Set<FileObject> instantiate (ProgressHandle handle) throws IOException {
        handle.start();
        try {
            handle.progress(Bundle.PRG_Dir());
            String[] splitlog = StringUtils.split(log, ":");
            ArchetypeWizardUtils.logUsage(splitlog[0], splitlog[1], splitlog[2]);
            return new LinkedHashSet<>(builder(handle).build());
        } finally {
            handle.finish();
        }
    }

    protected FileBuilder builder(ProgressHandle h) throws IOException {
        TemplateWizard w = (TemplateWizard)wiz;

        return new FileBuilder(w.getTemplate().getPrimaryFile(), w.getTargetFolder().getPrimaryFile().getParent()).
            param(TemplateUtils.PARAM_PACKAGE, (String) wiz.getProperty("package")).
            param(TemplateUtils.PARAM_PACKAGING, (String) this.packaging).
            param(TemplateUtils.PARAM_GROUP_ID, (String) wiz.getProperty("groupId")).
            param(TemplateUtils.PARAM_ARTIFACT_ID, (String) wiz.getProperty("artifactId")).
            param(TemplateUtils.PARAM_VERSION, (String) wiz.getProperty("version")).
            defaultMode(FileBuilder.Mode.COPY).
            name(w.getTargetName()).
            useLookup(Lookups.fixed(h));
    }
    
    /**
     * @deprecated Hook into {@link IDENativeTemplateHandler} instead.
     */
    @Deprecated
    protected CreateProjectBuilder createBuilder(File projFile, ProjectInfo vi, ProgressHandle handle) {
            CreateProjectBuilder builder = new CreateProjectBuilder(projFile, vi.groupId, vi.artifactId, vi.version)
                    .setProgressHandle(handle)
                    .setPackaging(packaging)
                    .setPackageName(vi.packageName);
            return builder;
    }
    
    @Override
    @Messages("LBL_CreateProjectStep2=Name and Location")
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        if (titlename != null) {
            wiz.putProperty ("NewProjectWizard_Title", titlename); // NOI18N        
        }
        index = 0;
        ValidationGroup vg = ValidationGroup.create(new WizardDescriptorAdapter(wiz));
        panels = new ArrayList<>();
        List<String> steps = new ArrayList<String>();
        
        panels.add(new BasicWizardPanel(vg, null, true, false, null)); //only download archetype (for additional props) when unknown archetype is used.
        steps.add(LBL_CreateProjectStep2());
        for (int i = 0; i < panels.size(); i++) {
            JComponent c = (JComponent) panels.get(i).getComponent();
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[0]));
      }
        
    }
    
    @Override
    public void uninitialize(WizardDescriptor wiz) {
//        wiz.putProperty(CommonProjectActions.PROJECT_PARENT_FOLDER, null); //NOI18N
        wiz.putProperty("name",null); //NOI18N
        this.wiz = null;
        panels = null;
    }
    
    @Messages({"# {0} - index", "# {1} - length", "NameFormat={0} of {1}"})
    public @Override String name() {
        return NameFormat(index + 1, panels.size());
    }
    
    @Override
    public boolean hasNext() {
        hasNextCalled.set(true);
        return hasNextImpl();        
    }
    
    private boolean hasNextImpl() {
        return index < panels.size() - 1;
    }
    
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }
    
    @Override
    public void nextPanel() {
        final boolean hnc = hasNextCalled.getAndSet(false);
        if (!hasNextImpl()) {
            throw new NoSuchElementException( //#216236
                    MessageFormat.format(
                    "index: {0}, panels: {1}, called has next: {2}",
                    index,
                    panels.size(),
                    hnc));
        }
        index++;
    }
    
    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels.get(index);
    }
    
    public @Override void addChangeListener(ChangeListener l) {}
    
    public @Override void removeChangeListener(ChangeListener l) {}

}
