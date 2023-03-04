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
package org.netbeans.modules.php.nette2.ui.customizer;

import java.util.EnumSet;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.nette2.preferences.Nette2Preferences;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class Nette2CustomizerExtender extends PhpModuleCustomizerExtender {
    private final boolean originalEnabled;
    private Nette2CustomizerPanel component;

    public Nette2CustomizerExtender(PhpModule phpModule) {
        originalEnabled = Nette2Preferences.isManuallyEnabled(phpModule);
    }

    @Override
    @NbBundle.Messages("LBL_CustomizerDisplayName=Nette2")
    public String getDisplayName() {
        return Bundle.LBL_CustomizerDisplayName();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
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
        return true;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public EnumSet<Change> save(PhpModule phpModule) {
        EnumSet<Change> result = null;
        boolean newEnabled = getPanel().isSupportEnabled();
        if (newEnabled != originalEnabled) {
            Nette2Preferences.setManuallyEnabled(phpModule, newEnabled);
            result = EnumSet.of(Change.FRAMEWORK_CHANGE);
        }
        return result;
    }

    private Nette2CustomizerPanel getPanel() {
        if (component == null) {
            component = new Nette2CustomizerPanel();
            component.setSupportEnabled(originalEnabled);
        }
        return component;
    }

}
