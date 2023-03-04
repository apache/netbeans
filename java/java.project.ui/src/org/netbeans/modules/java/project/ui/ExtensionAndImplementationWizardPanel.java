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
package org.netbeans.modules.java.project.ui;

import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Parameters;

/**
 * @author Arthur Sadykov
 */
public class ExtensionAndImplementationWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    private static final String ANNOTATION_TYPE_TEMPLATE_NAME = "AnnotationType";
    private static final String APPLET_TEMPLATE_NAME = "Applet";
    private static final String EMPTY_TEMPLATE_NAME = "Empty";
    private static final String ENUM_TEMPLATE_NAME = "Enum";
    private static final String INTERFACE_TEMPLATE_NAME = "Interface";
    private static final String JAPPLET_TEMPLATE_NAME = "JApplet";
    private static final String RECORD_TEMPLATE_NAME = "Record";
    private ExtensionAndImplementationVisualPanel component;
    private final WizardDescriptor wizardDescriptor;
    private final Set<ChangeListener> listeners = new HashSet<>(1);

    public ExtensionAndImplementationWizardPanel(WizardDescriptor wizardDescriptor) {
        Parameters.notNull("wizardDescriptor", wizardDescriptor); // NOI18N
        this.wizardDescriptor = wizardDescriptor;
    }

    WizardDescriptor getWizardDescriptor() {
        return wizardDescriptor;
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            FileObject template = Templates.getTemplate(wizardDescriptor);
            if (template != null) {
                switch (template.getName()) {
                    case ANNOTATION_TYPE_TEMPLATE_NAME:
                    case EMPTY_TEMPLATE_NAME: {
                        component = ExtensionAndImplementationVisualPanel.builder(this)
                                .build();
                        break;
                    }
                    case INTERFACE_TEMPLATE_NAME: {
                        component = ExtensionAndImplementationVisualPanel.builder(this)
                                .withExtensionBox()
                                .build();
                        break;
                    }
                    case APPLET_TEMPLATE_NAME:
                    case ENUM_TEMPLATE_NAME:
                    case JAPPLET_TEMPLATE_NAME:
                    case RECORD_TEMPLATE_NAME: {
                        component = ExtensionAndImplementationVisualPanel.builder(this)
                                .withImplementationBox()
                                .build();
                        break;
                    }
                    default: {
                        component = ExtensionAndImplementationVisualPanel.builder(this)
                                .withExtensionBox()
                                .withImplementationBox()
                                .build();
                    }
                }
            }
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void readSettings(WizardDescriptor wizardDescriptor) {
        if (component != null) {
            component.readSettings(wizardDescriptor);
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wizardDescriptor) {
        if (component != null) {
            component.storeSettings(wizardDescriptor);
        }
    }

    @Override
    public boolean isValid() {
        getComponent();
        if (component == null) {
            return false;
        }
        return component.isValid(wizardDescriptor);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        synchronized (listeners) {
            listeners.forEach(listener -> {
                listener.stateChanged(event);
            });
        }
    }
}
