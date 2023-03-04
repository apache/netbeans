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

package org.netbeans.modules.maven.newproject;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.validation.adapters.WizardDescriptorAdapter;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import static org.netbeans.modules.maven.newproject.Bundle.*;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 *@author mkleint
 */
@TemplateRegistration(folder=ArchetypeWizards.TEMPLATE_FOLDER, position=990, displayName="#template.pickArchetype", iconBase="org/netbeans/modules/maven/resources/Maven2Icon.gif", description="MavenDescription.html")
@Messages("template.pickArchetype=Project from Archetype")
public class MavenWizardIterator implements WizardDescriptor.BackgroundInstantiatingIterator<WizardDescriptor> {
    
    private static final long serialVersionUID = 1L;
    static final String PROPERTY_CUSTOM_CREATOR = "customCreator"; //NOI18N
    static final String PROP_ARCHETYPE = "archetype";
    private transient int index;
    private transient List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    private transient WizardDescriptor wiz;
    private final Archetype archetype;
    private final AtomicBoolean hasNextCalled = new AtomicBoolean(); //#216236
    private final String titlename;
    private final Map<String, String> defaultProps;

    public MavenWizardIterator() {
        this(null, null, null);
    }
    
    public MavenWizardIterator(Archetype archetype, String titleName, Map<String,String> defaultProps) {
        this.archetype = archetype;
        this.titlename = titleName;
        this.defaultProps = defaultProps;
    }

//    @TemplateRegistration(folder=ArchetypeWizards.TEMPLATE_FOLDER, position=100, displayName="#LBL_Maven_Quickstart_Archetype", iconBase="org/netbeans/modules/maven/resources/jaricon.png", description="quickstart.html")
//    @Messages("LBL_Maven_Quickstart_Archetype=Java Application")
//    public static WizardDescriptor.InstantiatingIterator<?> quickstart() {
//        return ArchetypeWizards.definedArchetype("org.apache.maven.archetypes", "maven-archetype-quickstart", "1.1", null, LBL_Maven_Quickstart_Archetype());
//    }
    

//    @TemplateRegistration(folder=ArchetypeWizards.TEMPLATE_FOLDER, position=980, displayName="#LBL_Maven_POM_Archetype", iconBase="org/netbeans/modules/maven/resources/Maven2Icon.gif", description="pom-root.html")
//    @Messages("LBL_Maven_POM_Archetype=POM Project")
//    public static WizardDescriptor.InstantiatingIterator<?> pomRoot() {
//        return ArchetypeWizards.definedArchetype("org.codehaus.mojo.archetypes", "pom-root", "1.1", null, LBL_Maven_POM_Archetype());
//    }

    public @Override Set<FileObject> instantiate() throws IOException {
        return ArchetypeWizardUtils.instantiate(wiz);
    }
    
    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        if (titlename != null) {
            wiz.putProperty ("NewProjectWizard_Title", titlename); // NOI18N        
        }
        index = 0;
        ValidationGroup vg = ValidationGroup.create(new WizardDescriptorAdapter(wiz));
        panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        List<String> steps = new ArrayList<String>();
        if (archetype == null) {
            panels.add(new ChooseWizardPanel());
            steps.add(LBL_CreateProjectStep());
        }
        panels.add(new BasicWizardPanel(vg, null, true, true, defaultProps)); //only download archetype (for additional props) when unknown archetype is used.
        steps.add(LBL_CreateProjectStep2());
        for (int i = 0; i < panels.size(); i++) {
            JComponent c = (JComponent) panels.get(i).getComponent();
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[0]));
      }
        if (archetype != null) {
            wiz.putProperty(PROP_ARCHETYPE, archetype);
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
