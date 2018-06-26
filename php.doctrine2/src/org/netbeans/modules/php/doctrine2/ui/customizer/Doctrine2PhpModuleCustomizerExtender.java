/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
