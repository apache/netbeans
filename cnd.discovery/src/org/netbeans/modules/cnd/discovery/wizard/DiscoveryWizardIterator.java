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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
