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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.entity;

import java.io.IOException;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.EntityGenerator;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.MultiTargetChooserPanel;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class EntityEJBWizard implements WizardDescriptor.InstantiatingIterator {

    private WizardDescriptor.Panel[] panels;
    private int index = 0;
    private EntityEJBWizardDescriptor ejbPanel;
    private WizardDescriptor wiz;

    public static EntityEJBWizard create () {
        return new EntityEJBWizard ();
    }

    public String name () {
        return NbBundle.getMessage (EntityEJBWizard.class, "LBL_EntityEJBWizardTitle");
    }

    public void uninitialize(WizardDescriptor wiz) {
    }

    public void initialize(WizardDescriptor wizardDescriptor) {
        wiz = wizardDescriptor;
        Project project = Templates.getProject(wiz);
        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);
        ejbPanel = new EntityEJBWizardDescriptor();
        WizardDescriptor.Panel wizardPanel = new MultiTargetChooserPanel(project,sourceGroups, ejbPanel, true);

        panels = new WizardDescriptor.Panel[] {wizardPanel};
        Wizards.mergeSteps(wiz, panels, null);

    }

    public Set instantiate () {
        boolean isCMP = ejbPanel.isCMP();
        EntityGenerator entityGenerator = EntityGenerator.create(
                Templates.getTargetName(wiz), 
                Templates.getTargetFolder(wiz), 
                ejbPanel.hasRemote(), 
                ejbPanel.hasLocal(), 
                isCMP, 
                ejbPanel.getPrimaryKeyClassName(),
                null
                );
        FileObject result = null;
        try {
            result = entityGenerator.generate();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result == null ? Collections.<FileObject>emptySet() : Collections.singleton(result);
    }

    public void addChangeListener(ChangeListener listener) {
    }

    public void removeChangeListener(ChangeListener listener) {
    }

    public boolean hasPrevious () {
        return index > 0;
    }

    public boolean hasNext () {
    return index < panels.length - 1;
    }

    public void nextPanel () {
        if (! hasNext ()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel () {
        if (! hasPrevious ()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }
}

