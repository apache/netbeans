/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.docker.ui.build2;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerfileDetail;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class BuildOptionsPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final RequestProcessor RP = new RequestProcessor("Parsing Dockerfile", 1);

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private BuildOptionsVisual component;

    private WizardDescriptor wizard;

    public BuildOptionsPanel() {
        super();
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public BuildOptionsVisual getComponent() {
        if (component == null) {
            component = new BuildOptionsVisual();
            component.addChangeListener(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @NbBundle.Messages({
        "MSG_NonExistingDockerfile=The Dockerfile does not exist.",
        "MSG_NonRelativeDockerfile=The Dockerfile must be inside the build context."
    })
    @Override
    public boolean isValid() {
        // clear the error message
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);

        String buildContext = (String) wizard.getProperty(BuildImageWizard.BUILD_CONTEXT_PROPERTY);
        String dockerfile = component.getDockerfile();
        if (dockerfile == null) {
            dockerfile = buildContext + "/" + DockerAction.DOCKER_FILE;
        } else {
            dockerfile = buildContext + "/" + dockerfile;
        }
        FileSystem fs = (FileSystem) wizard.getProperty(BuildImageWizard.FILESYSTEM_PROPERTY);
        FileObject fo = fs.getRoot().getFileObject(dockerfile);

        // the last check avoids entires like Dockerfile/ to be considered valid files
        if (fo == null || !fo.isData() || !dockerfile.endsWith(fo.getNameExt())) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_NonExistingDockerfile());
            return false;
        }
        FileObject build = fs.getRoot().getFileObject(buildContext);
        if (build == null || !FileUtil.isParentOf(build, fo)) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_NonRelativeDockerfile());
            return false;
        }

        // We start dockerfile parsing only if dockerfile is valid.
        // That's why this code is located here, not in readSettings
        Map<String, String> buildArguments = (Map<String, String>) wizard.getProperty(BuildImageWizard.BUILD_ARGUMENTS_PROPERTY);
        if (buildArguments == null) {
            RP.submit(new BuildArgsUpdaterTask(fo));
        }

        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        if (wizard == null) {
            wizard = wiz;
        }

        component.setBuildContext((String) wiz.getProperty(BuildImageWizard.BUILD_CONTEXT_PROPERTY));
        String dockerfile = (String) wiz.getProperty(BuildImageWizard.DOCKERFILE_PROPERTY);
        if (dockerfile == null) {
            dockerfile = DockerAction.DOCKER_FILE;
        }
        component.setDockerfile(dockerfile);
        Boolean pull = (Boolean) wiz.getProperty(BuildImageWizard.PULL_PROPERTY);
        component.setPull(pull != null ? pull : BuildImageWizard.PULL_DEFAULT);
        Boolean noCache = (Boolean) wiz.getProperty(BuildImageWizard.NO_CACHE_PROPERTY);
        component.setPull(noCache != null ? noCache : BuildImageWizard.NO_CACHE_DEFAULT);

        // XXX revalidate; is this bug?
        changeSupport.fireChange();
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty(BuildImageWizard.DOCKERFILE_PROPERTY, component.getDockerfile());
        wiz.putProperty(BuildImageWizard.PULL_PROPERTY, component.isPull());
        wiz.putProperty(BuildImageWizard.NO_CACHE_PROPERTY, component.isNoCache());
        wiz.putProperty(BuildImageWizard.BUILD_ARGUMENTS_PROPERTY, component.getBuildArgs());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    private static final Pattern MACRO_PATTERN = Pattern.compile("(?<!\\\\)\\$");

    private class BuildArgsUpdaterTask implements Runnable {

        private final FileObject fo;

        private DockerfileDetail dockerfileDetail;

        public BuildArgsUpdaterTask(FileObject fo) {
            this.fo = fo;
        }

        @Override
        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                Map<String, String> userDefinedBuildArgs = component.getBuildArgs();
                // Submit changes iff user haven't start filling arguments table yet.
                if (userDefinedBuildArgs == null || userDefinedBuildArgs.isEmpty()) {
                    Map<String, String> filtered = filterMacros(dockerfileDetail.getBuildArgs());
                    component.setBuildArgs(filtered);
                    wizard.putProperty(BuildImageWizard.BUILD_ARGUMENTS_PROPERTY, filtered);
                }
            } else {
                try {
                    // null is OK
                    DockerInstance instance = (DockerInstance) wizard.getProperty(BuildImageWizard.INSTANCE_PROPERTY);
                    dockerfileDetail = new DockerAction(instance).getDetail(fo);
                    SwingUtilities.invokeLater(this);
                } catch (IOException ex) {
                    Logger.getLogger(BuildOptionsPanel.class.getName()).log(Level.INFO, "Can't parse dockerfile: {0}", ex.toString());
                }
            }
        }
    }

    /**
     * Remove all ARGS that contain macros, i.e. SOMEARG=$ANOTHER. Macros like
     * PRICE=\$100 are valid.
     */
    private Map<String, String> filterMacros(Map<String, String> map) {
        return map.entrySet().stream()
                .filter((entry) -> !MACRO_PATTERN.matcher(entry.getValue()).find())
                .collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue()));
    }
}
