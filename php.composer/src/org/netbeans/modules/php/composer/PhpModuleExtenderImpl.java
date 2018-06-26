/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.composer;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.composer.commands.Composer;
import org.netbeans.modules.php.composer.options.ComposerOptions;
import org.netbeans.modules.php.composer.options.ComposerOptionsValidator;
import org.netbeans.modules.php.composer.output.model.ComposerPackage;
import org.netbeans.modules.php.composer.ui.PhpModuleExtenderPanel;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

public class PhpModuleExtenderImpl implements PhpModuleExtender {

    private volatile PhpModuleExtenderPanel panel = null;


    PhpModuleExtenderImpl() {
    }

    @Override
    public String getIdentifier() {
        return "Composer"; // NOI18N
    }

    @NbBundle.Messages("PhpModuleExtenderImpl.displayName=Composer")
    @Override
    public String getDisplayName() {
        return Bundle.PhpModuleExtenderImpl_displayName();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }

    @NonNull
    @Override
    public PhpModuleExtenderPanel getComponent() {
        if (panel == null) {
            // #236069
            panel = Mutex.EVENT.readAccess(new Mutex.Action<PhpModuleExtenderPanel>() {
                @Override
                public PhpModuleExtenderPanel run() {
                    return new PhpModuleExtenderPanel();
                }
            });
        }
        return panel;
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public boolean isValid() {
        return getErrorMessage() == null;
    }

    @NbBundle.Messages({
        "# {0} - error message",
        "PhpModuleExtenderImpl.error.composer=Composer Options: {0}",
    })
    @Override
    public String getErrorMessage() {
        ValidationResult validationResult = getValidationResult();
        if (validationResult.hasErrors()) {
            return Bundle.PhpModuleExtenderImpl_error_composer(validationResult.getErrors().get(0).getMessage());
        }
        if (validationResult.hasWarnings()) {
            return Bundle.PhpModuleExtenderImpl_error_composer(validationResult.getWarnings().get(0).getMessage());
        }
        return null;
    }

    @Override
    public String getWarningMessage() {
        return null;
    }

    @Override
    public Set<FileObject> extend(PhpModule phpModule) throws ExtendingException {
        List<ComposerPackage> selectedPackages = getComponent().getSelectedPackages();
        if (selectedPackages.isEmpty()) {
            return Collections.emptySet();
        }
        Composer composer;
        try {
            composer = Composer.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            assert false : ex.getLocalizedMessage();
            throw new ExtendingException(ex.getLocalizedMessage(), ex);
        }
        Future<Integer> task = composer.initIfNotPresent(phpModule);
        waitFinished(task);
        for (ComposerPackage composerPackage : selectedPackages) {
            task = composer.require(phpModule, composerPackage.asFullPackage());
            assert task != null;
            waitFinished(task);
        }
        return Collections.emptySet();
    }

    private ValidationResult getValidationResult() {
        if (getComponent().getSelectedPackages().isEmpty()) {
            // no packages selected => no validation
            return new ValidationResult();
        }
        return new ComposerOptionsValidator()
                .validate(ComposerOptions.getInstance())
                .getResult();
    }

    private void waitFinished(Future<Integer> task) throws ExtendingException {
        if (task == null) {
            return;
        }
        try {
            task.get(1, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex);
            throw new ExtendingException(ex.getLocalizedMessage(), ex);
        } catch (TimeoutException ex) {
            task.cancel(true);
        }
    }

    //~ Inner classes

    @ServiceProvider(service = Factory.class, path = Factory.EXTENDERS_PATH, position = 100)
    public static final class FactoryImpl implements Factory {

        @Override
        public PhpModuleExtender create() {
            return new PhpModuleExtenderImpl();
        }

    }

}
