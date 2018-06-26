/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.smarty;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.framework.BadgeIcon;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.api.queries.PhpVisibilityQuery;
import org.netbeans.modules.php.api.queries.Queries;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.smarty.editor.TplDataLoader;
import org.netbeans.modules.php.smarty.ui.notification.AutodetectionPanel;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author Martin Fousek
 */
public final class SmartyPhpFrameworkProvider extends PhpFrameworkProvider {

    protected static final RequestProcessor RP = new RequestProcessor(SmartyPhpFrameworkProvider.class);

    private static final Logger LOGGER = Logger.getLogger(SmartyPhpFrameworkProvider.class.getName());

    /** Preferences property if the given {@link PhpModule} contains Smarty framework or not. */
    public static final String PROP_SMARTY_AVAILABLE = "smarty-framework"; // NOI18N

    private static final String ICON_PATH = "org/netbeans/modules/php/smarty/resources/smarty-badge-8.png"; // NOI18N
    private static final SmartyPhpFrameworkProvider INSTANCE = new SmartyPhpFrameworkProvider();

    private final BadgeIcon badgeIcon;

    @PhpFrameworkProvider.Registration(position=300)
    public static SmartyPhpFrameworkProvider getInstance() {
        return INSTANCE;
    }

    private SmartyPhpFrameworkProvider() {
        super("Smarty PHP Web Framework", //NOI18N
                NbBundle.getMessage(SmartyPhpFrameworkProvider.class, "LBL_FrameworkName"),  //NOI18N
                NbBundle.getMessage(SmartyPhpFrameworkProvider.class, "LBL_FrameworkDescription")); //NOI18N

        badgeIcon = new BadgeIcon(
                ImageUtilities.loadImage(ICON_PATH),
                SmartyPhpFrameworkProvider.class.getResource("/" + ICON_PATH)); // NOI18N
    }

    @Override
    public BadgeIcon getBadgeIcon() {
        return badgeIcon;
    }

    /**
     * Checks if the given {@code FileObject} has extension registered in the
     * IDE as Smarty template extension (text/x-tpl).
     *
     * @param fo investigated file
     * @return {@code true} if the file has extension registered as Smarty template
     * extension, {@code false} otherwise
     */
    public static boolean hasSmartyTemplateExtension(FileObject fo) {
        return FileUtil.getMIMEType(fo, TplDataLoader.MIME_TYPE, null) != null;
    }

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
    public boolean isInPhpModule(final PhpModule phpModule) {
        Boolean enabled = getSmartyPropertyEnabled(phpModule);
        return enabled != null && enabled;
    }

    @Override
    public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {
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
        return null;
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
    public PhpModuleCustomizerExtender createPhpModuleCustomizerExtender(PhpModule phpModule) {
        return new SmartyPhpModuleCustomizerExtender(phpModule);
    }

    @NbBundle.Messages({
        "SmartyPhpFrameworkProvider.tit.smarty.template.autodetection=Smarty autodetection",
    })
    @Override
    public void phpModuleOpened(final PhpModule phpModule) {
        if (getSmartyPropertyEnabled(phpModule) == null) {
            try {
                ParserManager.parseWhenScanFinished(FileUtils.PHP_MIME_TYPE, new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        RP.post(new SmartyAutodetectionJob(phpModule));
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Try to locate Smarty templates files in given directory.
     * @param fo directory where to seek
     * @return {@code true} if any Smarty template found, {@code false} otherwise
     */
    private static boolean detectSmartyTemplate(FileObject fo, PhpVisibilityQuery visibilityQuery) {
        if (!fo.isValid() || !visibilityQuery.isVisible(fo)) {
            return false;
        }

        assert fo.isFolder();
        for (FileObject child : fo.getChildren()) {
            if (child.isFolder()) {
                if (detectSmartyTemplate(child, visibilityQuery)) {
                    return true;
                }
            } else if (hasSmartyTemplateExtension(child)) {
                return true;
            }
        }
        return false;
    }

    @CheckForNull
    @SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
    private static Boolean getSmartyPropertyEnabled(PhpModule phpModule) {
        Preferences preferences = phpModule.getPreferences(SmartyPhpFrameworkProvider.class, true);
        String available = preferences.get(PROP_SMARTY_AVAILABLE, null);
        if (available == null) {
            return null;
        } else {
            return Boolean.valueOf(available);
        }
    }

    private static class SmartyAutodetectionJob implements Runnable {

        private final PhpModule phpModule;

        public SmartyAutodetectionJob(PhpModule phpModule) {
            this.phpModule = phpModule;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            LOGGER.log(Level.FINEST, "Smarty templates autodetection started.");
            FileObject sourceDirectory = phpModule.getSourceDirectory();
            PhpVisibilityQuery visibilityQuery = Queries.getVisibilityQuery(phpModule);
            if (sourceDirectory != null && detectSmartyTemplate(sourceDirectory, visibilityQuery)) {
                NotificationDisplayer.getDefault().notify(
                        Bundle.SmartyPhpFrameworkProvider_tit_smarty_template_autodetection(),
                        NotificationDisplayer.Priority.LOW.getIcon(),
                        new AutodetectionPanel(phpModule),
                        new AutodetectionPanel(phpModule),
                        NotificationDisplayer.Priority.LOW);
            }
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "Smarty templates autodetection took {0}ms.", System.currentTimeMillis() - startTime);
            }
        }
    }
}
