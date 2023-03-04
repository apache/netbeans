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
package org.netbeans.modules.php.doctrine2;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.php.api.framework.BadgeIcon;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.doctrine2.annotations.odm.Doctrine2OdmAnnotationsProvider;
import org.netbeans.modules.php.doctrine2.annotations.orm.Doctrine2OrmAnnotationsProvider;
import org.netbeans.modules.php.doctrine2.commands.Doctrine2CommandSupport;
import org.netbeans.modules.php.doctrine2.preferences.Doctrine2Preferences;
import org.netbeans.modules.php.doctrine2.ui.actions.Doctrine2PhpModuleActionsExtender;
import org.netbeans.modules.php.doctrine2.ui.customizer.Doctrine2PhpModuleCustomizerExtender;
import org.netbeans.modules.php.doctrine2.ui.wizards.Doctrine2PhpModuleExtender;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * PHP framework provider for Doctrine2 PHP framework.
 */
public final class Doctrine2PhpFrameworkProvider extends PhpFrameworkProvider {

    private static final Doctrine2PhpFrameworkProvider INSTANCE = new Doctrine2PhpFrameworkProvider();
    private static final String ICON_PATH = "org/netbeans/modules/php/doctrine2/ui/resources/doctrine_badge_8.png"; // NOI18N

    private final BadgeIcon badgeIcon;


    @NbBundle.Messages({
        "LBL_FrameworkName=Doctrine2 PHP Web Framework",
        "LBL_FrameworkDescription=Doctrine2 PHP Web Framework"
    })
    private Doctrine2PhpFrameworkProvider() {
        super("Doctrine2 PHP Web Framework", Bundle.LBL_FrameworkName(), Bundle.LBL_FrameworkDescription()); // NOI18N
        badgeIcon = new BadgeIcon(
                ImageUtilities.loadImage(ICON_PATH),
                Doctrine2PhpFrameworkProvider.class.getResource("/" + ICON_PATH)); // NOI18N
    }

    @PhpFrameworkProvider.Registration(position=600)
    public static Doctrine2PhpFrameworkProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public BadgeIcon getBadgeIcon() {
        return badgeIcon;
    }

    @Override
    public boolean isInPhpModule(PhpModule phpModule) {
        return Doctrine2Preferences.isEnabled(phpModule);
    }

    @Override
    public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {
        return new Doctrine2PhpModuleExtender();
    }

    @Override
    public PhpModuleActionsExtender getActionsExtender(PhpModule phpModule) {
        return new Doctrine2PhpModuleActionsExtender();
    }

    @Override
    public PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule) {
        return null;
    }

    @Override
    public FrameworkCommandSupport getFrameworkCommandSupport(PhpModule phpModule) {
        return new Doctrine2CommandSupport(phpModule);
    }

    @Override
    public PhpModuleProperties getPhpModuleProperties(PhpModule phpModule) {
        return new PhpModuleProperties();
    }

    @Override
    public PhpModuleCustomizerExtender createPhpModuleCustomizerExtender(PhpModule phpModule) {
        return new Doctrine2PhpModuleCustomizerExtender(phpModule);
    }

    @Override
    public List<AnnotationCompletionTagProvider> getAnnotationsCompletionTagProviders(PhpModule phpModule) {
        return Arrays.asList(
                new Doctrine2OrmAnnotationsProvider(),
                new Doctrine2OdmAnnotationsProvider());
    }

}
