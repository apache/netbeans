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
package org.netbeans.modules.php.zend2;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.php.api.framework.BadgeIcon;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation;
import org.netbeans.modules.php.zend2.editor.Zend2EditorExtender;
import org.netbeans.modules.php.zend2.ui.actions.Zend2PhpModuleActionsExtender;
import org.netbeans.modules.php.zend2.ui.wizards.Zend2PhpModuleExtender;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public final class Zend2PhpFrameworkProvider extends PhpFrameworkProvider {

    private static final Logger LOGGER = Logger.getLogger(Zend2PhpFrameworkProvider.class.getName());

    @StaticResource
    private static final String ICON_PATH = "org/netbeans/modules/php/zend2/ui/resources/zend_badge_8.png"; // NOI18N
    private static final Zend2PhpFrameworkProvider INSTANCE = new Zend2PhpFrameworkProvider();

    private final BadgeIcon badgeIcon;


    @NbBundle.Messages({
        "Zend2PhpFrameworkProvider.framework.name=Zend2 PHP Web Framework",
        "Zend2PhpFrameworkProvider.framework.description=Zend2 PHP Web Framework"
    })
    private Zend2PhpFrameworkProvider() {
        super("Zend2 PHP Web Framework", // NOI18N
                Bundle.Zend2PhpFrameworkProvider_framework_name(),
                Bundle.Zend2PhpFrameworkProvider_framework_description());
        badgeIcon = new BadgeIcon(
                ImageUtilities.loadImage(ICON_PATH),
                Zend2PhpFrameworkProvider.class.getResource("/" + ICON_PATH)); // NOI18N
    }

    @PhpFrameworkProvider.Registration(position=199)
    public static Zend2PhpFrameworkProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public BadgeIcon getBadgeIcon() {
        return badgeIcon;
    }

    @Override
    public boolean isInPhpModule(PhpModule phpModule) {
        if (phpModule.isBroken()) {
            // broken project
            return false;
        }
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return false;
        }
        FileObject config = sourceDirectory.getFileObject("config/application.config.php"); // NOI18N
        return config != null && config.isData() && config.isValid();
    }

    @Override
    public ImportantFilesImplementation getConfigurationFiles2(PhpModule phpModule) {
        return new ConfigurationFiles(phpModule);
    }

    @Override
    public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {
        return new Zend2PhpModuleExtender();
    }

    @Override
    public PhpModuleProperties getPhpModuleProperties(PhpModule phpModule) {
        PhpModuleProperties properties = new PhpModuleProperties();
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            LOGGER.info("Source directory does not exist?!");
            return properties;
        }
        FileObject webRoot = sourceDirectory.getFileObject("public"); // NOI18N
        if (webRoot == null) {
            // #228244
            LOGGER.log(Level.INFO, "Public directory should exist in {0} but children are: {1}",
                    new Object[] {sourceDirectory, Arrays.toString(sourceDirectory.getChildren())});
            return properties;
        }
        return properties
                .setWebRoot(webRoot);
    }

    @Override
    public PhpModuleActionsExtender getActionsExtender(PhpModule phpModule) {
        return new Zend2PhpModuleActionsExtender();
    }

    @Override
    public PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule) {
        return null;
    }

    @Override
    public FrameworkCommandSupport getFrameworkCommandSupport(PhpModule phpModule) {
        return null;
    }

    @Override
    public EditorExtender getEditorExtender(PhpModule phpModule) {
        return new Zend2EditorExtender();
    }

}
