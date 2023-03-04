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

package org.netbeans.modules.j2ee.ejbcore.patterns;

import java.io.IOException;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.ejbcore.EjbGenerationUtil;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;


/** 
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class ServiceLocatorWizard implements WizardDescriptor.AsynchronousInstantiatingIterator {
    private WizardDescriptor.Panel[] panels;
    private int index = 0;
    private WizardDescriptor wiz;
    
    private static final String [] STEPS =
                                   new String [] { 
                                       NbBundle.getMessage (ServiceLocatorWizard.class, 
					     "LBL_SpecifyName")
                                   };
                               
    public String name () {
	return NbBundle.getMessage (ServiceLocatorWizard.class, 
			 	    "LBL_MessageServiceLocatorWizardTitle");
    }

    public void uninitialize(WizardDescriptor wiz) {
    }
    
    public void initialize(WizardDescriptor wizardDescriptor) {
        wiz = wizardDescriptor;
        Project project = Templates.getProject(wiz);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        panels = new WizardDescriptor.Panel[] {JavaTemplates.createPackageChooser(project,sourceGroups)};
        Wizards.mergeSteps(wiz, panels, STEPS);
    }
    
    public Set instantiate () throws IOException {
        FileObject pkg = Templates.getTargetFolder(wiz);
        String clsName = Templates.getTargetName(wiz);
        Project project = Templates.getProject(wiz);
        DataFolder dataFolder = DataFolder.findFolder(pkg);
        FileObject template = Templates.getTemplate(wiz);
        DataObject dTemplate = DataObject.find( template );                
        DataObject dobj = dTemplate.createFromTemplate( dataFolder, clsName);
        String pkgName = EjbGenerationUtil.getSelectedPackageName(pkg);
        String fullName = (pkgName.length()>0?pkgName+'.':"")+clsName;
        EnterpriseReferenceContainer erc = project.getLookup().lookup(EnterpriseReferenceContainer.class);
        if (erc != null) {
            erc.setServiceLocatorName(fullName);
        }
        FileObject createdFile = dobj.getPrimaryFile();
        //#156674
        ContainerClassPathModifier modifier = project.getLookup().lookup(ContainerClassPathModifier.class);
        if (modifier != null) {
            modifier.extendClasspath(dobj.getPrimaryFile(), new String[] {
                ContainerClassPathModifier.API_J2EE
            });
        }
        
        return Collections.singleton(createdFile); 
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
            throw new NoSuchElementException ();
        }
        index++;
    }
    
    public void previousPanel () {
        if (! hasPrevious ()) {
            throw new NoSuchElementException ();
        }
        index--;
    }
    
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    
}

