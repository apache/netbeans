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

package org.netbeans.modules.maven.newproject;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import static org.netbeans.modules.maven.newproject.Bundle.*;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

/**
 * Panel just asking for basic info.
 * @author mkleint
 */
public class UseOpenWizardPanel implements WizardDescriptor.Panel {
    
    private UseOpenPanel component;
    
    /** Creates a new instance of templateWizardPanel */
    public UseOpenWizardPanel() {
    }
    
    @Messages("TIT_UseOpenProjectStep=Open Maven project with Existing POM")
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new UseOpenPanel();
            component.setName(TIT_UseOpenProjectStep());
        }
        return component;
    }
    
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.maven.newproject.UseOpenWizardPanel");
    }
    
    @Override
    public boolean isValid() {
        return true;
    }
    
    @Override
    public final void addChangeListener(ChangeListener l) {}
    @Override
    public final void removeChangeListener(ChangeListener l) {}
    
    @Override
    public void readSettings(Object settings) {}
    
    @Override
    public void storeSettings(Object settings) {}
    
    public boolean isFinishPanel() {
        return true;
    }
    
}
