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

package org.netbeans.modules.java.freeform.ui;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.netbeans.modules.java.freeform.JavaProjectGenerator;
import org.netbeans.modules.java.freeform.JavaProjectNature;
import org.netbeans.modules.java.freeform.spi.support.NewJavaFreeformProjectSupport;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class SourceFoldersWizardPanel implements WizardDescriptor.Panel, ChangeListener, WizardDescriptor.FinishablePanel {

    private SourceFoldersPanel component;
    private WizardDescriptor wizardDescriptor;

    public SourceFoldersWizardPanel() {
        getComponent().setName(NbBundle.getMessage (NewJ2SEFreeformProjectWizardIterator.class, "TXT_NewJ2SEFreeformProjectWizardIterator_SourcePackageFolders"));
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new SourceFoldersPanel();
            component.setChangeListener(this);
            ((JComponent)component).getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(SourceFoldersWizardPanel.class, "ACSD_SourceFoldersWizardPanel")); // NOI18N            
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx( SourceFoldersWizardPanel.class );
    }
    
    public boolean isValid() {
        getComponent();
        // Panel is valid without any source folder specified, but
        // Next button is enabled only when there is some soruce 
        // folder specified -> see NewJ2SEFreeformProjectWizardIterator
        // which enables/disables Next button
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
        return true;
    }

    public boolean isFinishPanel() {
        return true;
    }
    
    private final ChangeSupport cs = new ChangeSupport(this);
    public final void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    public final void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    protected final void fireChangeEvent() {
        cs.fireChange();
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;
        File projectLocation = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_PROJECT_LOCATION);
        File projectFolder = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_PROJECT_FOLDER);
        PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(null, new PropertyProvider[]{
            PropertyUtils.fixedPropertyProvider(
            Collections.singletonMap(ProjectConstants.PROP_PROJECT_LOCATION, projectLocation.getAbsolutePath()))});

        ProjectModel pm = (ProjectModel)wizardDescriptor.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL);
        if (pm == null ||
                !pm.getBaseFolder().equals(projectLocation) ||
                !pm.getNBProjectFolder().equals(projectFolder)) {
            pm = ProjectModel.createEmptyModel(projectLocation, projectFolder, evaluator);
            wizardDescriptor.putProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL, pm);
        }
        List l = (List)wizardDescriptor.getProperty(NewJavaFreeformProjectSupport.PROP_EXTRA_JAVA_SOURCE_FOLDERS);
        if (l != null) {
            Iterator it = l.iterator();
            while (it.hasNext()) {
                String path = (String)it.next();
                assert it.hasNext();
                String label = (String)it.next();
                // try to find if the model already contains this source folder
                boolean found = false;
                for (int i = 0; i < pm.getSourceFoldersCount(); i++) {
                    JavaProjectGenerator.SourceFolder existingSf = pm.getSourceFolder(i);
                    if (existingSf.location.equals(path)) {
                        found = true;
                        break;
                    }
                }
                // don't add the folder if it is already in the model
                if (!found) {
                    JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
                    sf.location = path;
                    sf.label = label;
                    sf.type = JavaProjectConstants.SOURCES_TYPE_JAVA;
                    sf.style = JavaProjectNature.STYLE_PACKAGES;
                    pm.addSourceFolder(sf, false);
                }
            }
        }
        
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); // NOI18N
        component.setModel(pm, null);
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
    
}
