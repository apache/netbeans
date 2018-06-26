/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
