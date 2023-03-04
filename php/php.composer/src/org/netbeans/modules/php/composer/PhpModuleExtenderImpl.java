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
