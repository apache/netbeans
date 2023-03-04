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
package org.netbeans.modules.php.doctrine2.ui.customizer;

import java.util.EnumSet;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.doctrine2.preferences.Doctrine2Preferences;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Doctrine2 ustomizer extender, displayed always (for all PHP modules).
 */
public final class Doctrine2PhpModuleCustomizerExtender extends PhpModuleCustomizerExtender {

    private final boolean originalEnabled;

    private Doctrine2CustomizerPanel component;


    public Doctrine2PhpModuleCustomizerExtender(PhpModule phpModule) {
        originalEnabled = Doctrine2Preferences.isEnabled(phpModule);
    }

    @NbBundle.Messages("Doctrine2PhpModuleCustomizerExtender.Doctrine2=Doctrine2")
    @Override
    public String getDisplayName() {
        return Bundle.Doctrine2PhpModuleCustomizerExtender_Doctrine2();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        // not needed
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        // not needed
    }

    @Override
    public JComponent getComponent() {
        return getPanel();
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public boolean isValid() {
        return true; // always valid
    }

    @Override
    public String getErrorMessage() {
        return null; // always valid
    }

    @Override
    public EnumSet<Change> save(PhpModule phpModule) {
        boolean newEnabled = getPanel().isSupportEnabled();
        if (newEnabled != originalEnabled) {
            Doctrine2Preferences.setEnabled(phpModule, newEnabled);
            return EnumSet.of(Change.FRAMEWORK_CHANGE);
        }
        return null;
    }

    private Doctrine2CustomizerPanel getPanel() {
        if (component == null) {
            component = new Doctrine2CustomizerPanel();
            component.setSupportEnabled(originalEnabled);
        }
        return component;
    }

}
