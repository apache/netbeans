/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * Iterator for generation of @FacesComponents.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class FacesComponentIterator implements TemplateWizard.Iterator {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_COMPONENT_NS = "http://xmlns.jcp.org/jsf/component"; //NOI18N

    private transient WizardDescriptor.Panel[] panels;
    private int index;

    private FacesComponentPanel facesComponentPanel;

    @Override
    public Set<DataObject> instantiate(TemplateWizard wizard) throws IOException {
        FileObject targetFolder = Templates.getTargetFolder(wizard);
        String targetName = Templates.getTargetName(wizard);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);
        FileObject template = Templates.getTemplate(wizard);
        DataObject dTemplate = DataObject.find(template);
        Map<String, Object> templateProperties = new HashMap<String, Object>();

        String tagName = (String) wizard.getProperty(FacesComponentPanel.PROP_TAG_NAME);
        String tagNamespace = (String) wizard.getProperty(FacesComponentPanel.PROP_TAG_NAMESPACE);
        Boolean createSampleCode = (Boolean) wizard.getProperty(FacesComponentPanel.PROP_SAMPLE_CODE);
        if (!tagName.isEmpty() && !tagName.equals(tagNameForClassName(targetName))) {
            templateProperties.put("tagName", tagName); //NOI18N
        }
        if (!tagNamespace.isEmpty() && !tagNamespace.equals(DEFAULT_COMPONENT_NS)) {
            templateProperties.put("tagNamespace", tagNamespace); //NOI18N
        }
        if (createSampleCode) {
            templateProperties.put("sampleCode", Boolean.TRUE); //NOI18N
        }
        DataObject result = dTemplate.createFromTemplate(dataFolder, targetName, templateProperties);
        return Collections.singleton(result);
    }

    private static String tagNameForClassName(String className) {
        if (className.isEmpty()) {
            return ""; //NOI18N
        } else {
            return className.substring(0, 1).toLowerCase() + className.substring(1);
        }
    }

    @Override
    public void initialize(TemplateWizard wiz) {
        index = 0;
        Project project = Templates.getProject( wiz );
        panels = createPanels(project, wiz);

        // Creating steps.
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override
    public String name() {
        return NbBundle.getMessage(TemplateIterator.class, "TITLE_x_of_y", new Integer(index + 1), new Integer(panels.length)); //NOI18N
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    protected WizardDescriptor.Panel[] createPanels(Project project, TemplateWizard wiz) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        facesComponentPanel = new FacesComponentPanel(wiz);
        WizardDescriptor.Panel javaPanel;
        if (sourceGroups.length == 0) {
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(FacesComponentIterator.class, "MSG_No_Sources_found")); //NOI18N
            javaPanel = facesComponentPanel;
        } else {
            javaPanel = JavaTemplates.createPackageChooser(project, sourceGroups, facesComponentPanel);
        }
        return new WizardDescriptor.Panel[]{javaPanel};
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[(before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }
}
