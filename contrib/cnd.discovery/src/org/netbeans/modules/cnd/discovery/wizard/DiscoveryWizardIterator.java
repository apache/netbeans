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

package org.netbeans.modules.cnd.discovery.wizard;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.discovery.wizard.support.impl.DiscoveryProjectGeneratorImpl;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class DiscoveryWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {
    private static final RequestProcessor RP = new RequestProcessor(DiscoveryWizardIterator.class.getName(), 1);
    private DiscoveryWizardDescriptor wizard;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels ;
    private int index = 0;
    private boolean doClean = true;
    /** Creates a new instance of DiscoveryWizardIterator */
    public DiscoveryWizardIterator(WizardDescriptor.Panel<WizardDescriptor>[] panels) {
        this.panels = panels;
    }
    
    @Override
    public Set<?> instantiate() throws IOException {
        doClean = false;
        RP.post(new Runnable(){
            @Override
            public void run() {
                try {
                    DiscoveryProjectGeneratorImpl generator;
                    try {
                        generator = new DiscoveryProjectGeneratorImpl(wizard);
                        generator.makeProject();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    wizard.clean();
                    wizard = null;
                    panels = null;
                } catch (Throwable ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        return null;
    }
    
    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = (DiscoveryWizardDescriptor) wizard;
    }
    
    @Override
    public void uninitialize(WizardDescriptor wizard) {
        if (doClean) {
            DiscoveryWizardDescriptor wiz = (DiscoveryWizardDescriptor)wizard;
            wiz.clean();
            this.wizard = null;
            panels = null;
        }
    }
    
    @Override
    public synchronized WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];

    }
    
    @Override
    public String name() {
        return null;
    }
    
    @Override
    public synchronized boolean hasNext() {
        return index < (panels.length - 1);
    }
    
    @Override
    public synchronized boolean hasPrevious() {
        return index > 0;
    }
    
    @Override
    public synchronized void nextPanel() {
        if ((index + 1) == panels.length) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    @Override
    public synchronized void previousPanel() {
        if (index == 0) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
    }
    
    @Override
    public void removeChangeListener(ChangeListener l) {
    }
}
