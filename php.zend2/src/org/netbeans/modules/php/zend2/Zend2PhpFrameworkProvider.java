/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
