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

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.io.InputStream;
import java.util.Collection;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 *
 * @author Petr Pisl
 */
public class TemplateClientPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    private WizardDescriptor wizardDescriptor;
    private TemplateClientPanelVisual component;
    
    /** Creates a new instance of TemplateClientPanel */
    public TemplateClientPanel(WizardDescriptor wizardDescriptor) {
        component = null;
        this.wizardDescriptor = wizardDescriptor;
    }

    @Override
    public Component getComponent() {
        if (component == null){
            component = new TemplateClientPanelVisual(wizardDescriptor);
        }
        
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.jsf.wizards.TemplateClientPanel");
    }

    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
    }

    @Override
    public void storeSettings(Object settings) {
    }

    @Override
    public boolean isValid() {
        return component.validateTemplate();
    }

    public InputStream getTemplateClient(){
        getComponent();
        return component.getTemplateClient();    
    }
    
    public Collection<String> getTemplateData(){
        getComponent();
        return component.getTemplateData();
    }

    public Collection<String> getTemplateDataToGenerate(){
        getComponent();
        return component.getTemplateDataToGenerate();
    }
    
    public TemplateEntry getTemplate(){
        getComponent();
        return component.getTemplate();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        getComponent();
        component.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        getComponent();
        component.removeChangeListener(l);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    protected static final class TemplateEntry {
        private final boolean resourceLibraryContract;
        private final FileObject template;

        public TemplateEntry(FileObject template) {
            this(template, false);
        }

        public TemplateEntry(FileObject template, boolean resourceLibraryContract) {
            this.template = template;
            this.resourceLibraryContract = resourceLibraryContract;
        }

        public FileObject getTemplate() {
            return template;
        }

        public boolean isResourceLibraryContract() {
            return resourceLibraryContract;
        }
    }
    
}
