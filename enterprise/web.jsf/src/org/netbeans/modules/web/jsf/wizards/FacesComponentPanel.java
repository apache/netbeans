/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.jsf.wizards;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
class FacesComponentPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    protected static final String PROP_TAG_NAME = "tagName"; //NOI18N
    protected static final String PROP_TAG_NAMESPACE = "tagNamespace"; //NOI18N
    protected static final String PROP_SAMPLE_CODE = "sampleCode"; //NOI18N
    
    private final WizardDescriptor descriptor;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private FacesComponentPanelVisual gui;

    public FacesComponentPanel(WizardDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public FacesComponentPanelVisual getComponent() {
        if (gui == null) {
            gui = new FacesComponentPanelVisual();
        }
        return gui;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        getComponent();
        gui.addChangeListener(this);
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        getComponent();
        gui.removeChangeListener(this);

        settings.putProperty(PROP_TAG_NAME, gui.getTagName());
        settings.putProperty(PROP_TAG_NAMESPACE, gui.getTagNamespace());
        settings.putProperty(PROP_SAMPLE_CODE, gui.isSampleCode());
    }

    @Messages({
        "FacesComponentPanel.err.jsf.version.not.suficient=Minimal required JSF version for this feature is JSF 2.2"
    })
    @Override
    public boolean isValid() {
        getComponent();
        descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); //NOI18N

        Project project = Templates.getProject(descriptor);
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null) {
            JsfVersion jsfVersion = JsfVersionUtils.forWebModule(webModule);
            if (jsfVersion != null && !jsfVersion.isAtLeast(JsfVersion.JSF_2_2)) {
                descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.FacesComponentPanel_err_jsf_version_not_suficient());
                return false;
            }
        }
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }

    private void fireChangeEvent() {
        changeSupport.fireChange();
    }

}
