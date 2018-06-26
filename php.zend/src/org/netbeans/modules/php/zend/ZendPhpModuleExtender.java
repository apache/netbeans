/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.zend;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.zend.ui.wizards.NewProjectConfigurationPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class ZendPhpModuleExtender extends PhpModuleExtender {
    //@GuardedBy(this)
    private NewProjectConfigurationPanel panel = null;

    @Override
    public Set<FileObject> extend(PhpModule phpModule) throws ExtendingException {
        // init project
        ZendScript zendScript = null;
        try {
            zendScript = ZendScript.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            // should not happen, must be handled in the wizard
            Exceptions.printStackTrace(ex);
            throw new ExtendingException(ex.getLocalizedMessage(), ex);
        }

        if (!zendScript.initProject(phpModule)) {
            // can happen if zend script was not chosen
            Logger.getLogger(ZendPhpModuleExtender.class.getName())
                    .log(Level.INFO, "Framework Zend not found in newly created project {0}", phpModule.getDisplayName());
            throw new ExtendingException(NbBundle.getMessage(ZendPhpModuleExtender.class, "MSG_NotExtended"));
        }

        // prefetch commands
        ZendPhpFrameworkProvider.getInstance().getFrameworkCommandSupport(phpModule).refreshFrameworkCommandsLater(null);

        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            assert false : "Module extender for no sources of: " + phpModule.getName();
            return Collections.emptySet();
        }

        // return files
        Set<FileObject> files = new HashSet<>();
        FileObject appConfig = sourceDirectory.getFileObject("application/configs/application.ini"); // NOI18N
        if (appConfig != null) {
            files.add(appConfig);
        }
        FileObject indexController = sourceDirectory.getFileObject("application/controllers/IndexController.php"); // NOI18N
        if (indexController != null) {
            files.add(indexController);
        }
        FileObject bootstrap = sourceDirectory.getFileObject("application/Bootstrap.php"); // NOI18N
        if (bootstrap != null) {
            files.add(bootstrap);
        }

        return files;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getPanel().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getPanel().removeChangeListener(listener);
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
        return getErrorMessage() == null;
    }

    @Override
    public String getErrorMessage() {
        try {
            ZendScript.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            return NbBundle.getMessage(ZendPhpModuleExtender.class, "MSG_CannotExtend", ex.getMessage());
        }
        return getPanel().getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return getPanel().getWarningMessage();
    }

    private synchronized NewProjectConfigurationPanel getPanel() {
        if (panel == null) {
            panel = new NewProjectConfigurationPanel();
        }
        return panel;
    }
}
