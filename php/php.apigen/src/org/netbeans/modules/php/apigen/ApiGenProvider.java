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
package org.netbeans.modules.php.apigen;

import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.apigen.commands.ApiGenScript;
import org.netbeans.modules.php.apigen.ui.ApiGenPreferences;
import org.netbeans.modules.php.apigen.ui.customizer.PhpModuleCustomizerImpl;
import org.netbeans.modules.php.apigen.ui.options.ApiGenOptionsPanelController;
import org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleCustomizer;
import org.openide.util.NbBundle;

/**
 * {@link PhpDocProvider} for <a href="http://apigen.org/">ApiGen</a>.
 */
public final class ApiGenProvider extends PhpDocumentationProvider {

    private static final String LAST_FOLDER_SUFFIX = ".apiGen.dir"; // NOI18N

    private static final ApiGenProvider INSTANCE = new ApiGenProvider();


    @NbBundle.Messages("ApiGenProvider.name=ApiGen")
    private ApiGenProvider() {
        super("ApiGen", Bundle.ApiGenProvider_name()); // NOI18N
    }

    @PhpDocumentationProvider.Registration(position=90)
    public static ApiGenProvider getInstance() {
        return INSTANCE;
    }

    public static String lastDirFor(PhpModule phpModule) {
        return ApiGenProvider.class.getName() + LAST_FOLDER_SUFFIX + phpModule.getName();
    }

    @Override
    public boolean isInPhpModule(PhpModule phpModule) {
        return ApiGenPreferences.isEnabled(phpModule);
    }

    @Override
    public PhpModuleCustomizer createPhpModuleCustomizer(PhpModule phpModule) {
        return new PhpModuleCustomizerImpl(phpModule);
    }

    @Override
    public void generateDocumentation(PhpModule phpModule) {
        try {
            ApiGenScript.getDefault().generateDocumentation(phpModule);
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(ex.getLocalizedMessage(), ApiGenOptionsPanelController.OPTIONS_SUBPATH);
        }
    }

    @Override
    public void notifyEnabled(PhpModule phpModule, boolean enabled) {
        ApiGenPreferences.setEnabled(phpModule, enabled);
    }

}
