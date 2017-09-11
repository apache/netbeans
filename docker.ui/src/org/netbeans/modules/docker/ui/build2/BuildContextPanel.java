/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.build2;

import java.io.File;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.ui.Validations;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class BuildContextPanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private BuildContextVisual component;
    private final FileSystem fileSystem;
    private WizardDescriptor wizard;

    public BuildContextPanel(FileSystem fileSystem) {
        super();
        this.fileSystem = fileSystem;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public BuildContextVisual getComponent() {
        if (component == null) {
            component = new BuildContextVisual(fileSystem);
            component.addChangeListener(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isFinishPanel() {
        return BuildImageWizard.isFinishable((FileSystem) wizard.getProperty(BuildImageWizard.FILESYSTEM_PROPERTY),
                component.getBuildContext(),
                (String) wizard.getProperty(BuildImageWizard.DOCKERFILE_PROPERTY));
    }

    @NbBundle.Messages({
        "MSG_NonExistingBuildContext=The build context does not exist.",
        "MSG_EmptyRepository=The repository must not be empty when using tag."
    })
    @Override
    public boolean isValid() {
        // clear the error message
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);

        String buildContext = component.getBuildContext();
        FileSystem fs = (FileSystem) wizard.getProperty(BuildImageWizard.FILESYSTEM_PROPERTY);
        FileObject buildContextFo = buildContext == null ? null : fs.getRoot().getFileObject(buildContext);
        if (buildContext == null || buildContextFo == null || !buildContextFo.isFolder()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_NonExistingBuildContext());
            return false;
        }
        if (component.getRepository() == null && component.getTag() != null) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_EmptyRepository());
            return false;
        }
        String repository = component.getRepository();
        if (repository != null) {
            String message = Validations.validateRepository(repository);
            if (message != null) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
                return false;
            }
        }
        String tag = component.getTag();
        if (tag != null) {
            String message = Validations.validateTag(tag);
            if (message != null) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
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
    public void readSettings(WizardDescriptor wiz) {
        if (wizard == null) {
            wizard = wiz;
        }

        component.setBuildContext((String) wiz.getProperty(BuildImageWizard.BUILD_CONTEXT_PROPERTY));
        component.setRepository((String) wiz.getProperty(BuildImageWizard.REPOSITORY_PROPERTY));
        component.setTag((String) wiz.getProperty(BuildImageWizard.TAG_PROPERTY));

        // XXX revalidate; is this bug?
        changeSupport.fireChange();
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty(BuildImageWizard.BUILD_CONTEXT_PROPERTY, component.getBuildContext());
        wiz.putProperty(BuildImageWizard.REPOSITORY_PROPERTY, component.getRepository());
        wiz.putProperty(BuildImageWizard.TAG_PROPERTY, component.getTag());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }
}
