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
package org.netbeans.modules.web.wizards;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.NbBundle;

/**
 * Validator for PageIterator panels.
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class PageIteratorValidation {

    /**
     * Validates JSF/JSP file wizard.
     */
    public static class JsfJspValidatorPanel extends DelegatingWizardDescriptorPanel {

        public JsfJspValidatorPanel(Panel delegate) {
            super(delegate);
        }

        @NbBundle.Messages({
            "JsfJspValidatorPanel.warn.document.root=Project has no valid DocumentRoot"
        })
        @Override
        public boolean isValid() {
            Project project = getProject();
            if (super.isValid()) {
                // check that that project has valid document root
                WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
                if (webModule != null && webModule.getDocumentBase() == null) {
                    getWizardDescriptor().putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, Bundle.JsfJspValidatorPanel_warn_document_root());
                }
                return true;
            }
            return false;
        }

    }
}
