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
package org.netbeans.modules.cnd.makeproject.api.ui.wizard;

import java.util.List;
import org.netbeans.modules.cnd.makeproject.ui.wizards.MakeSampleProjectIterator;
import org.netbeans.modules.cnd.makeproject.ui.wizards.NewMakeProjectWizardIterator;
import org.openide.WizardDescriptor;

/**
 *
 */
public final class ProjectWizardPanels {

    private ProjectWizardPanels() {
    }
    
    public static List<WizardDescriptor.Panel<WizardDescriptor>> getNewProjectWizardPanels(int wizardtype, String name, String wizardTitle, String wizardACSD, boolean fullRemote) {
        return NewMakeProjectWizardIterator.getPanels(wizardtype, name, wizardTitle, wizardACSD, fullRemote);
    }

    public static MakeSamplePanel<WizardDescriptor> getMakeSampleProjectWizardPanel(int wizardtype, String name, String wizardTitle, 
            String wizardACSD, boolean fullRemote) {
        return getMakeSampleProjectWizardPanel(wizardtype, name, wizardTitle, wizardACSD, fullRemote, null);
    }
    
    public static MakeSamplePanel<WizardDescriptor> getMakeSampleProjectWizardPanel(int wizardtype, String name, String wizardTitle, 
            String wizardACSD, boolean fullRemote, String helpCtx) {
        return MakeSampleProjectIterator.getPanel(wizardtype, name, wizardTitle, wizardACSD, fullRemote, helpCtx);
    }
    
    
    public static MakeModePanel<WizardDescriptor> getSelectModePanel() {
        return  NewMakeProjectWizardIterator.createSelectModePanel();
    }

    public static WizardDescriptor.Panel<WizardDescriptor> getSelectBinaryPanel() {
        return  NewMakeProjectWizardIterator.getSelectBinaryPanel();
    }

    public interface MakeSamplePanel<T> extends WizardDescriptor.FinishablePanel<T> {
        void setFinishPanel(boolean isFinishPanel);
    }

    public interface MakeModePanel<T> extends MakeSamplePanel<T> {
    }

    public interface NamedPanel {
        String getName();
    }
}
