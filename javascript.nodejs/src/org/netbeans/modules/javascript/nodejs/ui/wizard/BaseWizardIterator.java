/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.ui.wizard;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

abstract class BaseWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    @StaticResource
    protected static final String NODEJS_PROJECT_ICON = "org/netbeans/modules/javascript/nodejs/ui/resources/new-nodejs-project.png"; // NOI18N

    protected volatile WizardDescriptor wizardDescriptor;

    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;


    abstract String getWizardTitle();

    abstract WizardDescriptor.Panel<WizardDescriptor>[] createPanels();

    abstract String[] createSteps();

    abstract void uninitializeInternal();

    @Override
    public final void initialize(WizardDescriptor wizard) {
        wizardDescriptor = wizard;
        // #245975
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                initialize();
            }
        });
    }

    final void initialize() {
        assert EventQueue.isDispatchThread();
        index = 0;
        panels = createPanels();
        String[] steps = createSteps();
        // XXX should be lazy
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert steps[i] != null : "Missing name for step: " + i;
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                // name
                jc.setName(steps[i]);
            }
        }
    }

    @Override
    public final void uninitialize(WizardDescriptor wizard) {
        panels = null;
        uninitializeInternal();
    }

    @Override
    public final Set<FileObject> instantiate() throws IOException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        wizardDescriptor.putProperty("NewProjectWizard_Title", getWizardTitle()); // NOI18N
        return panels[index];
    }

    @NbBundle.Messages({
        "# {0} - current step index",
        "# {1} - number of steps",
        "BaseWizardIterator.name={0} of {1}"
    })
    @Override
    public String name() {
        return Bundle.BaseWizardIterator_name(index + 1, panels.length);
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        // noop
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        // noop
    }

}
