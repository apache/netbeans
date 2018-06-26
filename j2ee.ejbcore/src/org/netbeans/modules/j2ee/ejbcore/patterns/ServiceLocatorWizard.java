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

