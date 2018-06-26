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
package org.netbeans.modules.web.client.samples.wizard.ui;

import java.io.File;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class SamplePanel implements Panel<WizardDescriptor> {

    private SampleVisualPanel myPanel;
    private WizardDescriptor myDescriptor;


    public SamplePanel(WizardDescriptor descriptor) {
        myDescriptor = descriptor;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
    }

    @Override
    public SampleVisualPanel getComponent() {
        if (myPanel == null) {
            myPanel = new SampleVisualPanel(myDescriptor);
        }
        return myPanel;
    }

    @Override
    public HelpCtx getHelp() {
        return null;
        //return new HelpCtx("html5.samples"); // NOI18N
    }

    @Override
    public boolean isValid() {
        String error = getComponent().getErrorMessage();
        if (error != null && !error.isEmpty()) {
            setErrorMessage(error);
            return false;
        }
        // everything ok
        setErrorMessage(null);
        return true;
    }

    @Override
    public void readSettings(WizardDescriptor descriptor) {
        descriptor.putProperty("NewProjectWizard_Title", NbBundle.getMessage(
                SamplePanel.class, "TTL_SamplePanel"));         // NOI18N
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }

    @Override
    public void storeSettings(WizardDescriptor descriptor) {
        File projectLocation = new File(getComponent().getProjectLocation());
        FileObject directory = FileUtil.toFileObject(FileUtil.normalizeFile(projectLocation));
        Templates.setTargetFolder(myDescriptor, directory);
        Templates.setTargetName(myDescriptor, getComponent().getProjectName());
    }

    private void setErrorMessage(String message) {
        myDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }
}
