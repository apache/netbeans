/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.symfony;

import org.netbeans.modules.php.api.framework.BadgeIcon;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender;
import org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation;
import org.netbeans.modules.php.symfony.commands.SymfonyCommandSupport;
import org.netbeans.modules.php.symfony.editor.SymfonyEditorExtender;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class SymfonyPhpFrameworkProvider extends PhpFrameworkProvider {
    public static final String FILE_CONFIG = "config/ProjectConfiguration.class.php"; // NOI18N

    private static final String ICON_PATH = "org/netbeans/modules/php/symfony/ui/resources/symfony_badge_8.png"; // NOI18N
    private static final SymfonyPhpFrameworkProvider INSTANCE = new SymfonyPhpFrameworkProvider();

    private final BadgeIcon badgeIcon;

    @PhpFrameworkProvider.Registration(position=100)
    public static SymfonyPhpFrameworkProvider getInstance() {
        return INSTANCE;
    }

    private SymfonyPhpFrameworkProvider() {
        super("Symfony PHP Web Framework", // NOI18N
                NbBundle.getMessage(SymfonyPhpFrameworkProvider.class, "LBL_FrameworkName"),
                NbBundle.getMessage(SymfonyPhpFrameworkProvider.class, "LBL_FrameworkDescription"));
        badgeIcon = new BadgeIcon(
                ImageUtilities.loadImage(ICON_PATH),
                SymfonyPhpFrameworkProvider.class.getResource("/" + ICON_PATH)); // NOI18N
    }

    @Override
    public BadgeIcon getBadgeIcon() {
        return badgeIcon;
    }

    /**
     * Try to locate (find) a <code>relativePath</code> in source directory.
     * Currently, it searches source dir and its subdirs (if <code>subdirs</code> equals {@code true}).
     * @return {@link FileObject} or {@code null} if not found
     */
    public static FileObject locate(PhpModule phpModule, String relativePath, boolean subdirs) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return null;
        }

        FileObject fileObject = sourceDirectory.getFileObject(relativePath);
        if (fileObject != null || !subdirs) {
            return fileObject;
        }
        for (FileObject child : sourceDirectory.getChildren()) {
            fileObject = child.getFileObject(relativePath);
            if (fileObject != null) {
                return fileObject;
            }
        }
        return null;
    }

    @Override
    public boolean isInPhpModule(PhpModule phpModule) {
        // #170775
        // first search "symfony", then "config/ProjectConfiguration.class.php"
        FileObject symfony = locate(phpModule, SymfonyScript.SCRIPT_NAME, false);
        if (symfony != null && symfony.isData()) {
            return true;
        }
        FileObject config = locate(phpModule, FILE_CONFIG, true);
        return config != null && config.isData();
    }

    @Override
    public ImportantFilesImplementation getConfigurationFiles2(PhpModule phpModule) {
        return new ConfigurationFiles(phpModule);
    }

    @Override
    public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {
//        return new SymfonyPhpModuleExtender();
        // legacy version, disable it
        return null;
    }

    @Override
    public PhpModuleCustomizerExtender createPhpModuleCustomizerExtender(PhpModule phpModule) {
        if (isInPhpModule(phpModule)) {
            return new SymfonyPhpModuleCustomizerExtender(phpModule);
        }
        return null;
    }

    @Override
    public PhpModuleProperties getPhpModuleProperties(PhpModule phpModule) {
        PhpModuleProperties properties = new PhpModuleProperties();
        FileObject web = locate(phpModule, "web", true); // NOI18N
        if (web != null) {
            properties = properties.setWebRoot(web);
        }
        FileObject testUnit = locate(phpModule, "test/unit", true); // NOI18N
        if (testUnit != null) {
            properties = properties.setTests(testUnit);
        }
        return properties;
    }

    @Override
    public PhpModuleActionsExtender getActionsExtender(PhpModule phpModule) {
        return new SymfonyPhpModuleActionsExtender();
    }

    @Override
    public PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule) {
        return new SymfonyPhpModuleIgnoredFilesExtender(phpModule);
    }

    @Override
    public SymfonyCommandSupport getFrameworkCommandSupport(PhpModule phpModule) {
        return new SymfonyCommandSupport(phpModule);
    }

    @Override
    public EditorExtender getEditorExtender(PhpModule phpModule) {
        return new SymfonyEditorExtender();
    }
}
