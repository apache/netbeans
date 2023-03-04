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

package org.netbeans.modules.php.phpdoc;

import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.phpdoc.ui.PhpDocPreferences;
import org.netbeans.modules.php.phpdoc.ui.customizer.PhpModuleCustomizerImpl;
import org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleCustomizer;
import org.openide.util.NbBundle;

public final class PhpDocumentorProvider extends PhpDocumentationProvider {
    public static final String PHPDOC_LAST_FOLDER_SUFFIX = ".phpdoc.dir"; // NOI18N

    private static final PhpDocumentorProvider INSTANCE = new PhpDocumentorProvider();

    private PhpDocumentorProvider() {
        super("phpDocumentor", NbBundle.getMessage(PhpDocumentorProvider.class, "LBL_Name")); // NOI18N
    }

    @PhpDocumentationProvider.Registration(position=100)
    public static PhpDocumentorProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isInPhpModule(PhpModule phpModule) {
        return PhpDocPreferences.isEnabled(phpModule);
    }

    @Override
    public PhpModuleCustomizer createPhpModuleCustomizer(PhpModule phpModule) {
        return new PhpModuleCustomizerImpl(phpModule);
    }

    @Override
    public void generateDocumentation(PhpModule phpModule) {
        try {
            PhpDocScript phpDocScript = PhpDocScript.getForPhpModule(phpModule, true);
            if (phpDocScript != null) {
                phpDocScript.generateDocumentation(phpModule);
            }
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(ex.getLocalizedMessage(), PhpDocScript.OPTIONS_SUB_PATH);
        }
    }

    @Override
    public void notifyEnabled(PhpModule phpModule, boolean enabled) {
        PhpDocPreferences.setEnabled(phpModule, enabled);
    }

}
