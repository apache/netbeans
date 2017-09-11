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

package org.netbeans.modules.java.examples;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 * @author Martin Grebac
 */
public class J2SESampleProjectIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {

    private static final long serialVersionUID = 4L;

    int currentIndex;
    PanelConfigureProject basicPanel;
    private transient WizardDescriptor wiz;

    static Object create() {
        return new J2SESampleProjectIterator();
    }
    
    public J2SESampleProjectIterator () {
    }
    
    public void addChangeListener (javax.swing.event.ChangeListener changeListener) {
    }
    
    public void removeChangeListener (javax.swing.event.ChangeListener changeListener) {
    }
    
    public org.openide.WizardDescriptor.Panel current () {
        return basicPanel;
    }
    
    public boolean hasNext () {
        return false;
    }
    
    public boolean hasPrevious () {
        return false;
    }
    
    @Override public void initialize(WizardDescriptor templateWizard) {
        this.wiz = templateWizard;
        String displayName;
        try {
            displayName = DataObject.find(Templates.getTemplate(wiz)).getNodeDelegate().getDisplayName();
        } catch (DataObjectNotFoundException ex) {
            displayName = "unknown";
        }
        String name = displayName;
        if (name != null) {
            name = name.replaceAll(" ", ""); //NOI18N
        }
        templateWizard.putProperty (WizardProperties.NAME, name);        
        basicPanel = new PanelConfigureProject(displayName);
        currentIndex = 0;
        updateStepsList ();
    }
    
    @Override public void uninitialize(WizardDescriptor templateWizard) {
        basicPanel = null;
        currentIndex = -1;
        this.wiz.putProperty("projdir",null);           //NOI18N
        this.wiz.putProperty("name",null);          //NOI18N
    }
    
    @Override public Set instantiate() throws java.io.IOException {
        File projectLocation = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        FileObject templateFO = Templates.getTemplate(wiz);
        FileObject prjLoc = J2SESampleProjectGenerator.createProjectFromTemplate(
                              templateFO, projectLocation, name);

        java.util.Set set = new java.util.HashSet();
        set.add(DataObject.find(prjLoc));

        // open file from the project specified in the "defaultFileToOpen" attribute
        Object openFile = templateFO.getAttribute("defaultFileToOpen"); // NOI18N
        if (openFile instanceof String) {
            FileObject openFO = prjLoc.getFileObject((String)openFile);
            set.add(DataObject.find(openFO));
        }
        // also open a documentation file registered for this project
        // and copy the .url file for it to the project (#71985)
        FileObject docToOpen = FileUtil.getConfigFile(
            "org-netbeans-modules-java-examples/OpenAfterCreated/" + templateFO.getName() + ".url"); // NOI18N
        if (docToOpen != null) {
            docToOpen = FileUtil.copyFile(docToOpen, prjLoc, "readme"); // NOI18N
            set.add(DataObject.find(docToOpen));
        }

        return set;
    }
    
    public String name() {
        return current().getComponent().getName();
    }
    
    public void nextPanel() {
        throw new NoSuchElementException ();
    }
    
    public void previousPanel() {
        throw new NoSuchElementException ();
    }
    
    void updateStepsList() {
        JComponent component = (JComponent) current ().getComponent ();
        if (component == null) {
            return;
        }
        String[] list;
        list = new String[] {
            NbBundle.getMessage(PanelConfigureProject.class, "LBL_NWP1_ProjectTitleName"), // NOI18N
        };
        component.putClientProperty (WizardDescriptor.PROP_CONTENT_DATA, list); // NOI18N
        component.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer (currentIndex)); // NOI18N
    }
    
}
