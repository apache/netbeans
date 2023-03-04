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
package org.netbeans.modules.php.project.ui.wizards;

import java.io.IOException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;

public final class NewFileWizardIteratorWithNamespace implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {

    private final NewFileWizardIterator delegate = NewFileWizardIterator.withNamespace();

    private NewFileWizardIteratorWithNamespace() {
    }

    public static NewFileWizardIteratorWithNamespace create() {
        return new NewFileWizardIteratorWithNamespace();
    }

    @Override
    public Set instantiate() throws IOException {
        return delegate.instantiate();
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        delegate.initialize(wizard);
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        delegate.uninitialize(wizard);
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return delegate.current();
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return delegate.hasPrevious();
    }

    @Override
    public void nextPanel() {
        delegate.nextPanel();
    }

    @Override
    public void previousPanel() {
        delegate.previousPanel();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        delegate.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        delegate.removeChangeListener(l);
    }

}
