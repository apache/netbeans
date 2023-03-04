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
package org.netbeans.modules.php.nette2;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.php.api.framework.BadgeIcon;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.nette2.annotations.Nette2AnnotationsProvider;
import org.netbeans.modules.php.nette2.preferences.Nette2Preferences;
import org.netbeans.modules.php.nette2.ui.actions.Nette2PhpModuleActionsExtender;
import org.netbeans.modules.php.nette2.ui.customizer.Nette2CustomizerExtender;
import org.netbeans.modules.php.nette2.utils.Constants;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class Nette2FrameworkProvider extends PhpFrameworkProvider {
    private static final Nette2FrameworkProvider INSTANCE = new Nette2FrameworkProvider();
    private final BadgeIcon badgeIcon;

    @PhpFrameworkProvider.Registration(position = 190)
    public static Nette2FrameworkProvider getInstance() {
        return INSTANCE;
    }

    @NbBundle.Messages({
        "LBL_FrameworkName=Nette2 PHP Web Framework",
        "LBL_FrameworkDescription=Nette2 PHP Web Framework"
    })
    private Nette2FrameworkProvider() {
        super("Nette2 PHP Web Framework", Bundle.LBL_FrameworkName(), Bundle.LBL_FrameworkDescription()); //NOI18N
        badgeIcon = new BadgeIcon(
                ImageUtilities.loadImage(Constants.ICON_PATH),
                Nette2FrameworkProvider.class.getResource("/" + Constants.ICON_PATH)); //NOI18N
    }

    @Override
    public boolean isInPhpModule(PhpModule phpModule) {
        boolean result = Nette2Preferences.isManuallyEnabled(phpModule);
        if (!result) {
            FileObject sourceDirectory = phpModule.getSourceDirectory();
            if (sourceDirectory != null) {
                FileObject bootstrap = getFileObject(sourceDirectory, Constants.COMMON_BOOTSTRAP_PATH);
                result = bootstrap != null && !bootstrap.isFolder() && bootstrap.isValid();
                FileObject config = getFileObject(sourceDirectory, Constants.COMMON_CONFIG_PATH);
                result = result && config != null && config.isFolder() && config.isValid();
            }
        }
        return result;
    }

    @Override
    public ImportantFilesImplementation getConfigurationFiles2(PhpModule phpModule) {
        return new ConfigurationFiles(phpModule);
    }

    @Override
    public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {
        return new Nette2PhpModuleExtender();
    }

    @Override
    public PhpModuleProperties getPhpModuleProperties(PhpModule phpModule) {
        return new PhpModuleProperties();
    }

    @Override
    public PhpModuleActionsExtender getActionsExtender(PhpModule phpModule) {
        return new Nette2PhpModuleActionsExtender();
    }

    @Override
    public PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule) {
        return new PhpModuleIgnoredFilesExtender() {
            @Override
            public Set<File> getIgnoredFiles() {
                return Collections.<File>emptySet();
            }
        };
    }

    @Override
    public FrameworkCommandSupport getFrameworkCommandSupport(PhpModule phpModule) {
        return null;
    }

    @Override
    public BadgeIcon getBadgeIcon() {
        return badgeIcon;
    }

    @Override
    public List<AnnotationCompletionTagProvider> getAnnotationsCompletionTagProviders(PhpModule phpModule) {
        return Collections.<AnnotationCompletionTagProvider>singletonList(new Nette2AnnotationsProvider());
    }

    @Override
    public PhpModuleCustomizerExtender createPhpModuleCustomizerExtender(PhpModule phpModule) {
        return new Nette2CustomizerExtender(phpModule);
    }

    /**
     * Try to get a FileObject with correct filename case. See bug 238679.
     *
     * @param parent Parent FileObject.
     * @param relPath Relative path, separated by slashes.
     */
    private FileObject getFileObject(FileObject parent, String relPath) {
        File parentFile = FileUtil.toFile(parent);
        if (parentFile != null) {
            String nativePath = relPath.replace('/', File.separatorChar);
            File file = new File(parentFile, nativePath);
            return FileUtil.toFileObject(FileUtil.normalizeFile(file));
        } else {
            return null;
        }
    }
}
