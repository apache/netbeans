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

package org.netbeans.modules.javaee.beanvalidation;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author alexeybutenko
 */
public class ConstraintIterator implements TemplateWizard.Iterator{
    
    private transient WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private int index;
    private final String VALIDATOR_TEMPLATE = "Validator.java"; //NOI18N
    
    @Override
    public Set<DataObject> instantiate(TemplateWizard wizard) throws IOException {
        FileObject dir = Templates.getTargetFolder( wizard );
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wizard );

        DataObject dTemplate = DataObject.find( template );
        List<String> targetElements = (List<String>) wizard.getProperty(WizardProperties.TARGET_ELEMENTS);
        if (targetElements == null) {
            targetElements = new ArrayList<String>();
        }
        String validatorClassName = (String) wizard.getProperty(WizardProperties.VALIDATOR_CLASS_NAME);
        String validatorType = (String)wizard.getProperty(WizardProperties.VALIDATOR_TYPE);
        boolean generateValidator = validatorClassName !=null;

        HashMap<String, Object> templateProperties = new HashMap<String, Object>();
        templateProperties.put(WizardProperties.TARGET_ELEMENTS, targetElements);
        if (generateValidator) {
            templateProperties.put(WizardProperties.VALIDATOR_CLASS_NAME, validatorClassName);
            templateProperties.put(WizardProperties.VALIDATOR_TYPE, validatorType);
        }
        DataObject dobj = null;
        String constraintClass = wizard.getTargetName();
        dobj = dTemplate.createFromTemplate(df, constraintClass, templateProperties);
        if (generateValidator) {
            FileObject validatorTemplate = template.getParent().getFileObject(VALIDATOR_TEMPLATE);
            DataObject validatorDataObject = createValidator(df, validatorTemplate, validatorClassName, validatorType, constraintClass);
            if (validatorDataObject !=null) {
                return new HashSet<DataObject>(Arrays.asList(new DataObject[]{dobj, validatorDataObject}));
            }
        }
        return Collections.singleton(dobj);
    }

    @Override
    public void initialize(TemplateWizard wizard) {
        index = 0;

        Project project = Templates.getProject(wizard);
        final WizardDescriptor.Panel<WizardDescriptor> constraintPanel = new ConstraintPanel(wizard);
        SourceGroup[] sourceGroup = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        WizardDescriptor.Panel javaPanel;
        if (sourceGroup.length == 0) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(ConstraintIterator.class, "MSG_No_Sources_found"));
            javaPanel = constraintPanel;
        } else {
            javaPanel = JavaTemplates.createPackageChooser(project, sourceGroup, constraintPanel, true);
            javaPanel.addChangeListener((ConstraintPanel)constraintPanel);
        }
        panels = new WizardDescriptor.Panel[] { javaPanel };

        // Creating steps.
        Object prop = wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }

    @Override
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }

    @Override
    public Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @Override
    public String name() {
        return NbBundle.getMessage (ConstraintIterator.class, "TITLE_x_of_y", index + 1, panels.length);
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index>0;
    }

    @Override
    public void nextPanel() {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }

    @Override
    public void previousPanel() {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    private static DataObject createValidator(DataFolder df, FileObject template, String className, String type, String constraintClass) {
        try {
            DataObject dTemplate = DataObject.find(template);
            HashMap<String, Object> templateProperties = new HashMap<String, Object>();
            templateProperties.put(WizardProperties.VALIDATOR_TYPE, type);
            templateProperties.put(WizardProperties.CONSTRAINT_CLASS_NAME, constraintClass);
            return dTemplate.createFromTemplate(df, className,templateProperties);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    static class WizardProperties {
        public static final String CONSTRAINT_CLASS_NAME = "constraint";    //NOI18N
        public static final String VALIDATOR_CLASS_NAME = "validator";    //NOI18N
        public static final String VALIDATOR_TYPE = "validatorType";    //NOI18N
        public static final String TARGET_ELEMENTS = "targetElements";    //NOI18N
    }
}
