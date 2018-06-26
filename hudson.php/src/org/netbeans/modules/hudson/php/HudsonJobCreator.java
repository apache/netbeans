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
package org.netbeans.modules.hudson.php;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory.Helper;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory.ProjectHudsonJobCreator;
import org.netbeans.modules.hudson.php.options.HudsonOptions;
import org.netbeans.modules.hudson.php.options.HudsonOptionsValidator;
import org.netbeans.modules.hudson.php.ui.options.HudsonOptionsPanelController;
import org.netbeans.modules.hudson.php.util.BuildXmlUtils;
import org.netbeans.modules.hudson.php.util.PhpUnitUtils;
import org.netbeans.modules.hudson.php.util.XmlUtils;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.hudson.spi.HudsonSCM.ConfigurationStatus;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public final class HudsonJobCreator extends JPanel implements ProjectHudsonJobCreator, ChangeListener {

    private static final long serialVersionUID = 1657613213547587L;

    private static final Logger LOGGER = Logger.getLogger(HudsonJobCreator.class.getName());

    private final PhpModule phpModule;
    private final HudsonSCM.Configuration scm;
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    private HudsonJobCreator(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
        scm = Helper.prepareSCM(FileUtil.toFile(phpModule.getProjectDirectory()));
    }

    private static HudsonJobCreator forPhpModule(PhpModule phpModule) {
        HudsonJobCreator hudsonJobCreator = new HudsonJobCreator(phpModule);
        // listeners
        HudsonOptions options = HudsonOptions.getInstance();
        options.addChangeListener(WeakListeners.change(hudsonJobCreator, options));
        return hudsonJobCreator;
    }

    @Override
    public String jobName() {
        return phpModule.getDisplayName();
    }

    @Override
    public JComponent customizer() {
        return this;
    }

    @NbBundle.Messages({
        "HudsonJobCreator.error.noTests=The project does not have any tests.",
        "HudsonJobCreator.error.invalidHudsonOptions=PHP Hudson options are invalid.",
        "# {0} - file name",
        "HudsonJobCreator.warning.fileExists=Existing project file {0} will be used.",
    })
    @Override
    public ConfigurationStatus status() {
        if (phpModule.getTestDirectories().isEmpty()) {
            return ConfigurationStatus.withError(Bundle.HudsonJobCreator_error_noTests());
        }
        if (scm == null) {
            return Helper.noSCMError();
        }
        // ide options
        if (HudsonOptionsValidator.validate(getDefaultBuildXml(), getDefaultJobConfig(), getDefaultPhpUnitConfig()) != null) {
            return ConfigurationStatus.withError(Bundle.HudsonJobCreator_error_invalidHudsonOptions()).withExtraButton(getOpenHudsonOptionsButton());
        }
        // scm
        ConfigurationStatus scmStatus = scm.problems();
        if (scmStatus != null) {
            return scmStatus;
        }
        return ConfigurationStatus.valid();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public Document configure() throws IOException {
        try {
            createBuildXml();
            createPhpUnitConfig();
        } catch (IOException ex) {
            throw new SilentIOException(ex);
        }
        return createJobXml();
    }

    @CheckForNull
    private String getDefaultBuildXml() {
        return HudsonOptions.getInstance().getBuildXml();
    }

    @CheckForNull
    private String getDefaultJobConfig() {
        return HudsonOptions.getInstance().getJobConfig();
    }

    @CheckForNull
    private String getDefaultPhpUnitConfig() {
        return HudsonOptions.getInstance().getPhpUnitConfig();
    }

    @CheckForNull
    private FileObject getProjectBuildXml() {
        return phpModule.getProjectDirectory().getFileObject(HudsonOptionsValidator.BUILD_XML_NAME);
    }

    @CheckForNull
    private FileObject getProjectPhpUnitConfig() {
        FileObject projectDirectory = phpModule.getProjectDirectory();
        FileObject phpUnitConfig = projectDirectory.getFileObject(HudsonOptionsValidator.PHP_UNIT_CONFIG_NAME);
        if (phpUnitConfig != null) {
            return phpUnitConfig;
        }
        return projectDirectory.getFileObject(HudsonOptionsValidator.PHP_UNIT_CONFIG_DIST_NAME);
    }

    @NbBundle.Messages({
        "HudsonJobCreator.button.labelWithMnemonics=&Hudson Options...",
        "HudsonJobCreator.button.a11y=Open Hudson PHP options."
    })
    private JButton getOpenHudsonOptionsButton() {
        JButton button = new JButton();
        Mnemonics.setLocalizedText(button, Bundle.HudsonJobCreator_button_labelWithMnemonics());
        button.getAccessibleContext().setAccessibleDescription(Bundle.HudsonJobCreator_button_a11y());
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UiUtils.showOptions(HudsonOptionsPanelController.OPTIONS_SUBPATH);
            }
        });
        return button;
    }

    private Document createJobXml() throws IOException {
        Document document;
        try {
            String defaultJobConfig = getDefaultJobConfig();
            assert defaultJobConfig != null;
            document = XmlUtils.parse(new File(defaultJobConfig));
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        // remove scm, triggers & logRotator if present
        removeNodes(document, "/project/scm", "/project/logRotator"); // NOI18N
        // configure
        scm.configure(document);
        Helper.addLogRotator(document);
        // enable
        Node disabled = XmlUtils.query(document, "/project/disabled"); // NOI18N
        if (disabled != null) {
            XmlUtils.setNodeValue(document, disabled, "false"); // NOI18N
        }
        return document;
    }

    private void removeNodes(Document document, String... xpathExpressions) {
        for (String xpathExpression : xpathExpressions) {
            Node node = XmlUtils.query(document, xpathExpression);
            if (node != null) {
                node.getParentNode().removeChild(node);
            }
        }
    }

    @NbBundle.Messages("HudsonJobCreator.buildXml.exist=Build script found in project, verify its content.")
    private void createBuildXml() throws IOException {
        FileObject buildXml = getProjectBuildXml();
        if (buildXml != null) {
            // existing build script will be used
            informUser(Bundle.HudsonJobCreator_buildXml_exist(), NotifyDescriptor.INFORMATION_MESSAGE);
            return;
        }
        Path projectBuildXml = new File(FileUtil.toFile(phpModule.getProjectDirectory()), HudsonOptionsValidator.BUILD_XML_NAME).toPath();
        Files.copy(Paths.get(getDefaultBuildXml()), projectBuildXml);
        BuildXmlUtils.processBuildXml(phpModule, projectBuildXml);
    }

    @NbBundle.Messages("HudsonJobCreator.phpUnitConfig.exist=PHPUnit configuration file found in project, verify its content.")
    private void createPhpUnitConfig() throws IOException {
        FileObject phpUnitConfig = getProjectPhpUnitConfig();
        if (phpUnitConfig != null) {
            // existing phpunit config will be used
            informUser(Bundle.HudsonJobCreator_phpUnitConfig_exist(), NotifyDescriptor.INFORMATION_MESSAGE);
            return;
        }
        Path projectPhpUnitConfig = new File(FileUtil.toFile(phpModule.getProjectDirectory()), HudsonOptionsValidator.PHP_UNIT_CONFIG_DIST_NAME).toPath();
        Files.copy(Paths.get(getDefaultPhpUnitConfig()), projectPhpUnitConfig);
        PhpUnitUtils.processPhpUnitConfig(phpModule, projectPhpUnitConfig);
    }

    private void informUser(String message, int messageType) {
        NotifyDescriptor descriptor = new NotifyDescriptor.Message(message, messageType);
        DialogDisplayer.getDefault().notify(descriptor);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // change in PHP Hudson Options
        changeSupport.fireChange();
    }

    //~ Inner classes

    @ServiceProvider(service=ProjectHudsonJobCreatorFactory.class, position=300)
    public static class Factory implements ProjectHudsonJobCreatorFactory {

        @Override
        public ProjectHudsonJobCreator forProject(Project project) {
            PhpModule phpModule = project.getLookup().lookup(PhpModule.class);
            if (phpModule == null) {
                // not a php project
                return null;
            }
            return HudsonJobCreator.forPhpModule(phpModule);
        }

    }

}
