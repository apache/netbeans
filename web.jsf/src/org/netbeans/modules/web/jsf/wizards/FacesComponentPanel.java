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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
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
            JSFVersion jsfVersion = JSFVersion.forWebModule(webModule);
            if (jsfVersion != null && !jsfVersion.isAtLeast(JSFVersion.JSF_2_2)) {
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
