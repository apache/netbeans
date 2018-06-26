/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cloud.amazon.ui;

import org.netbeans.modules.cloud.common.spi.support.ui.CloudResourcesWizardPanel;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cloud.amazon.AmazonInstance;
import org.netbeans.modules.cloud.amazon.AmazonInstanceManager;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.ChangeSupport;

/**
 *
 */
public class AmazonWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private ChangeSupport listeners;
    private WizardDescriptor wizard;
    private AmazonWizardPanel panel;
    private CloudResourcesWizardPanel panel2;
    boolean first = true;

    public AmazonWizardIterator() {
        listeners = new ChangeSupport(this);
    }
    
    public static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N

    @Override
    public Set instantiate() throws IOException {
        String keyId = (String)wizard.getProperty(AmazonWizardPanel.KEY_ID);
        assert keyId != null;
        String key = (String)wizard.getProperty(AmazonWizardPanel.KEY);
        assert key != null;
        String name = (String)wizard.getProperty(PROP_DISPLAY_NAME);
        assert name != null;
        String regionUrl = (String)wizard.getProperty(AmazonWizardPanel.REGION);
        
        AmazonInstanceManager.getDefault().add(new AmazonInstance(name, keyId, key, regionUrl));
        
        return Collections.emptySet();
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panel = null;
    }

    @Override
    public Panel current() {
        if (first) {
            if (panel == null) {
                panel = new AmazonWizardPanel();
            }
            return panel;
        } else {
            if (panel2 == null) {
                panel2 = new CloudResourcesWizardPanel(AmazonWizardPanel.getPanelContentData(), 1);
            }
            return panel2;
        }
    }

    @Override
    public String name() {
        return "Amazon";
    }

    @Override
    public boolean hasNext() {
        return first;
    }

    @Override
    public boolean hasPrevious() {
        return !first;
    }

    @Override
    public void nextPanel() {
        first = false;
    }

    @Override
    public void previousPanel() {
        first = true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.removeChangeListener(l);
    }
    
}
