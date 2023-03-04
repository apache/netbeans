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

package org.netbeans.modules.server.test;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.server.ServerRegistry;
import org.netbeans.spi.server.ServerWizardProvider;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class MockWizardProvider implements ServerWizardProvider {

    private final String wizardName;

    public MockWizardProvider(String wizardName) {
        this.wizardName = wizardName;
    }

    public static void registerWizardProvider(String instanceName, ServerWizardProvider provider) throws IOException {
        if (provider == null) {
            return;
        }

        Lookup.getDefault().lookup(ModuleInfo.class);

        FileObject servers = FileUtil.getConfigFile(ServerRegistry.SERVERS_PATH);
        FileObject testProvider = FileUtil.createData(servers, instanceName);

        testProvider.setAttribute("instanceOf", ServerWizardProvider.class.getName()); // NOI18N
        testProvider.setAttribute("instanceCreate", provider); // NOI18N
    }

    public String getDisplayName() {
        return wizardName;
    }

    public InstantiatingIterator getInstantiatingIterator() {
        return new MockWizardIterator(wizardName);
    }

    private static class MockWizardIterator implements InstantiatingIterator {

        private final String name;

        private Panel panel;

        public MockWizardIterator(String name) {
            this.name = name;
        }

        public Set instantiate() throws IOException {
            return Collections.EMPTY_SET;
        }

        public String name() {
            return name;
        }

        public synchronized Panel current() {
            if (panel == null) {
                panel = new MockWizardPanel(name);
            }
            return panel;
        }

        public boolean hasNext() {
            return false;
        }

        public boolean hasPrevious() {
            return false;
        }

        public void initialize(WizardDescriptor wizard) {
        }

        public void uninitialize(WizardDescriptor wizard) {
        }

        public void nextPanel() {
        }

        public void previousPanel() {
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }
    }

    private static class MockWizardPanel implements Panel {

        private final String name;

        private JPanel panel;

        public MockWizardPanel(String name) {
            this.name = name;
        }

        public synchronized Component getComponent() {
            if (panel == null) {
                panel = new JPanel();
                panel.add(new JLabel(name));
            }
            return panel;
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        public boolean isValid() {
            return true;
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public void readSettings(Object settings) {
        }

        public void storeSettings(Object settings) {
        }

    }
}
