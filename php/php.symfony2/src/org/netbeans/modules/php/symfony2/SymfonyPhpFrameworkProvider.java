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
package org.netbeans.modules.php.symfony2;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.framework.BadgeIcon;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender;
import org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation;
import org.netbeans.modules.php.symfony2.annotations.extra.SymfonyExtraAnnotationsProvider;
import org.netbeans.modules.php.symfony2.annotations.security.SymfonySecurityAnnotationsProvider;
import org.netbeans.modules.php.symfony2.annotations.validators.SymfonyValidatorsAnnotationsProvider;
import org.netbeans.modules.php.symfony2.commands.SymfonyCommandSupport;
import org.netbeans.modules.php.symfony2.preferences.SymfonyPreferences;
import org.netbeans.modules.php.symfony2.ui.actions.SymfonyPhpModuleActionsExtender;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * PHP framework provider for Symfony 2/3 PHP framework.
 */
public final class SymfonyPhpFrameworkProvider extends PhpFrameworkProvider {

    private static final Logger LOGGER = Logger.getLogger(SymfonyPhpFrameworkProvider.class.getName());

    private static final SymfonyPhpFrameworkProvider INSTANCE = new SymfonyPhpFrameworkProvider();
    private static final String ICON_PATH = "org/netbeans/modules/php/symfony2/ui/resources/symfony_badge_8.png"; // NOI18N

    private final BadgeIcon badgeIcon;


    @NbBundle.Messages({
        "LBL_FrameworkName=Symfony 2/3 PHP Web Framework",
        "LBL_FrameworkDescription=Symfony 2/3 PHP Web Framework"
    })
    private SymfonyPhpFrameworkProvider() {
        super("Symfony 2/3 PHP Web Framework", Bundle.LBL_FrameworkName(), Bundle.LBL_FrameworkDescription()); // NOI18N
        badgeIcon = new BadgeIcon(
                ImageUtilities.loadImage(ICON_PATH),
                SymfonyPhpFrameworkProvider.class.getResource("/" + ICON_PATH)); // NOI18N
    }

    @PhpFrameworkProvider.Registration(position = 99) // right before Symfony1
    public static SymfonyPhpFrameworkProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName(PhpModule phpModule) {
        SymfonyVersion symfonyVersion = SymfonyVersion.forPhpModule(phpModule);
        if (symfonyVersion != null) {
            return symfonyVersion.getFrameworkName(false);
        }
        return super.getName(phpModule);
    }

    @Override
    public BadgeIcon getBadgeIcon() {
        return badgeIcon;
    }

    @Override
    public boolean isInPhpModule(PhpModule phpModule) {
        Boolean enabled = SymfonyPreferences.isEnabled(phpModule);
        if (enabled != null) {
            // set manually
            return enabled;
        }
        // autodetection
        return SymfonyVersion.forPhpModule(phpModule) != null;
    }

    @Override
    public ImportantFilesImplementation getConfigurationFiles2(PhpModule phpModule) {
        return new ConfigurationFiles(phpModule);
    }

    @Override
    public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {
        return new SymfonyPhpModuleExtender();
    }

    @Override
    public PhpModuleCustomizerExtender createPhpModuleCustomizerExtender(PhpModule phpModule) {
        return new SymfonyPhpModuleCustomizerExtender(phpModule);
    }

    @Override
    public PhpModuleProperties getPhpModuleProperties(PhpModule phpModule) {
        PhpModuleProperties properties = new PhpModuleProperties();
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return properties;
        }
        FileObject web = sourceDirectory.getFileObject("web"); // NOI18N
        if (web != null) {
            properties = properties.setWebRoot(web);
        }
        SymfonyVersion symfonyVersion = SymfonyVersion.forPhpModule(phpModule);
        if (symfonyVersion == null) {
            // #267818 - incorrect symfony installer file
            LOGGER.log(Level.INFO, "No Symfony version detected for project {0} - perhaps invalid Symfony installer selected in IDE Options?", phpModule.getDisplayName());
        } else {
            FileObject tests = symfonyVersion.getTests();
            if (tests != null) {
                properties = properties.setTests(tests);
            }
        }
        return properties;
    }

    @Override
    public PhpModuleActionsExtender getActionsExtender(PhpModule phpModule) {
        return new SymfonyPhpModuleActionsExtender(phpModule);
    }

    @Override
    public PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule) {
        return new SymfonyPhpModuleIgnoredFilesExtender(phpModule);
    }

    @Override
    public FrameworkCommandSupport getFrameworkCommandSupport(PhpModule phpModule) {
        return new SymfonyCommandSupport(phpModule);
    }

    @Override
    public List<AnnotationCompletionTagProvider> getAnnotationsCompletionTagProviders(PhpModule phpModule) {
        return Arrays.<AnnotationCompletionTagProvider>asList(new SymfonyExtraAnnotationsProvider(),
                new SymfonySecurityAnnotationsProvider(),
                new SymfonyValidatorsAnnotationsProvider());
    }

}
