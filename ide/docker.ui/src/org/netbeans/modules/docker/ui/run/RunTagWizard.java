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
package org.netbeans.modules.docker.ui.run;

import org.netbeans.modules.docker.api.PortMapping;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerImage;
import org.netbeans.modules.docker.api.DockerImageDetail;
import org.netbeans.modules.docker.api.DockerTag;
import org.netbeans.modules.docker.api.DockerException;
import org.netbeans.modules.docker.api.DockerAction;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.netbeans.modules.docker.api.ActionStreamResult;
import org.netbeans.modules.docker.ui.output.OutputUtils;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Petr Hejl
 */
public class RunTagWizard {

    public static final String NAME_PROPERTY = "name";

    public static final String COMMAND_PROPERTY = "command";

    public static final String USER_PROPERTY = "user";

    public static final String INTERACTIVE_PROPERTY = "interactive";

    public static final String TTY_PROPERTY = "tty";
    
    public static final String PRIVILEGED_PROPERTY = "privileged";
    
    public static final String VOLUMES_PROPERTY = "mountVolumes";
    
    public static final String VOLUMES_TABLE_PROPERTY = "volumesTable";

    public static final String RANDOM_BIND_PROPERTY = "portRandom";

    public static final String PORT_MAPPING_PROPERTY = "portMapping";

    public static final boolean RANDOM_BIND_DEFAULT = false;

    private static final Logger LOGGER = Logger.getLogger(RunTagWizard.class.getName());

    private final DockerTag tag;

    public RunTagWizard(DockerTag tag) {
        this.tag = tag;
    }

    @NbBundle.Messages({
        "MSG_ReceivingImageInfo=Receiving Image Details",
        "LBL_Run=Run {0}"
    })
    public void show() {
        DockerImageDetail info = BaseProgressUtils.showProgressDialogAndRun(
                new DockerImageInfoRunnable(tag.getImage()), Bundle.MSG_ReceivingImageInfo(), false);

        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new RunContainerPropertiesPanel(info));
        panels.add(new RunPortBindingsPanel(info));
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            JComponent c = (JComponent) panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            c.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
        }
        final WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(Bundle.LBL_Run(getImage(tag)));
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            run(tag, wiz);
        }
    }

    private void run(final DockerTag tag, final WizardDescriptor wiz) {
        final Boolean portRandom = (Boolean) wiz.getProperty(RANDOM_BIND_PROPERTY);
        List<PortMapping> mappingVar = (List<PortMapping>) wiz.getProperty(PORT_MAPPING_PROPERTY);
        if (mappingVar == null) {
            mappingVar = Collections.emptyList();
        }
        final List<PortMapping> mapping = mappingVar;
        final String name = (String) wiz.getProperty(NAME_PROPERTY);
        final String command = (String) wiz.getProperty(COMMAND_PROPERTY);
        final String user = (String) wiz.getProperty(USER_PROPERTY);
        final boolean interactive = (Boolean) wiz.getProperty(INTERACTIVE_PROPERTY);
        final boolean tty = (Boolean) wiz.getProperty(TTY_PROPERTY);
        final boolean privileged = (Boolean) wiz.getProperty(PRIVILEGED_PROPERTY);
        final boolean randomBind = portRandom != null ? portRandom : RANDOM_BIND_DEFAULT;
        final boolean mountVolumes = (Boolean) wiz.getProperty(VOLUMES_PROPERTY);
        Map<String, String> volumesTableVar = (Map<String, String>) wiz.getProperty(VOLUMES_TABLE_PROPERTY);
        if (volumesTableVar == null) {
            volumesTableVar = new HashMap<>();
        }
        final Map<String, String> volumesTable = volumesTableVar;

        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    DockerAction remote = new DockerAction(tag.getImage().getInstance());
                    JSONObject config = new JSONObject();
                    if (user != null) {
                        config.put("User", user);
                    }
                    if (interactive) {
                        config.put("OpenStdin", true);
                        config.put("StdinOnce", true);
                        config.put("AttachStdin", true);
                    }
                    if (tty) {
                        config.put("Tty", true);
                    }

                    String[] parsed = command == null ? new String[]{} : Utilities.parseParameters(command);
                    config.put("Image", getImage(tag));
                    JSONArray cmdArray = new JSONArray();
                    cmdArray.addAll(Arrays.asList(parsed));
                    config.put("Cmd", cmdArray);
                    config.put("AttachStdout", true);
                    config.put("AttachStderr", true);
                    Map<String, List<PortMapping>> bindings = new HashMap<>();
                    for (PortMapping m : mapping) {
                        String str = m.getPort() + "/" + m.getType().name().toLowerCase(Locale.ENGLISH);
                        List<PortMapping> list = bindings.get(str);
                        if (list == null) {
                            list = new ArrayList<>();
                            bindings.put(str, list);
                        }
                        list.add(m);
                    }

                    JSONObject hostConfig = new JSONObject();
                    config.put("HostConfig", hostConfig);
                    if (privileged) {
                        hostConfig.put("Privileged", true);
                    }
                    hostConfig.put("PublishAllPorts", randomBind);
                    if (!randomBind && !bindings.isEmpty()) {
                        JSONObject portBindings = new JSONObject();
                        hostConfig.put("PortBindings", portBindings);

                        for (Map.Entry<String, List<PortMapping>> e : bindings.entrySet()) {
                            JSONArray arr = new JSONArray();
                            for (PortMapping m : e.getValue()) {
                                JSONObject o = new JSONObject();
                                o.put("HostIp", m.getHostAddress());
                                o.put("HostPort", m.getHostPort() != null ? m.getHostPort().toString() : "");
                                arr.add(o);
                            }
                            portBindings.put(e.getKey(), arr);
                        }
                    }
                    if (mountVolumes) {
                        JSONArray binds = new JSONArray();
                        hostConfig.put("Binds", binds);
                        for (String target : volumesTable.keySet()) {
                            binds.add(volumesTable.get(target) + ":" + target);
                        }
                    }
                    Pair<DockerContainer, ActionStreamResult> result = remote.run(name, config);

                    OutputUtils.openTerminal(result.first(), result.second(), interactive, true, null);
                } catch (Exception ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    String msg = ex.getLocalizedMessage();
                    NotifyDescriptor desc = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                }
            }
        });
    }

    private static String getImage(DockerTag tag) {
        String id = tag.getTag();
        if (id.equals("<none>:<none>")) { // NOI18N
            id = tag.getImage().getId();
        }
        return id;
    }

    private static class DockerImageInfoRunnable implements ProgressRunnable<DockerImageDetail> {

        private final DockerImage image;

        public DockerImageInfoRunnable(DockerImage image) {
            this.image = image;
        }

        @Override
        public DockerImageDetail run(ProgressHandle handle) {
            try {
                DockerAction remote = new DockerAction(image.getInstance());
                return remote.getDetail(image);
            } catch (DockerException ex) {
                return null;
            }
        }

    }
}
