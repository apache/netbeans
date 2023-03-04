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
package org.netbeans.modules.maven.hints.pom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class ReleaseVersionError implements POMErrorFixProvider {
    private final Configuration configuration;

    static final String PROP_RELEASE = "release";//NOI18N
    static final String PROP_LATEST = "latest";//NOI18N
    static final String PROP_SNAPSHOT = "snapshot";//NOI18N
    private JComponent component;

    public ReleaseVersionError() {
        configuration = new Configuration("ReleaseVersionError", //NOI18N
                org.openide.util.NbBundle.getMessage(ReleaseVersionError.class, "TIT_ReleaseLatestVersion"),
                org.openide.util.NbBundle.getMessage(ReleaseVersionError.class, "DESC_ReleaseLatestVersion"),
                true, Configuration.HintSeverity.WARNING);
    }

    @Override
    public List<ErrorDescription> getErrorsForDocument(POMModel model, Project prj) {
        assert model != null;
        List<ErrorDescription> toRet = new ArrayList<ErrorDescription>();
        boolean release = getConfiguration().getPreferences().getBoolean(PROP_RELEASE, true);
        boolean latest = getConfiguration().getPreferences().getBoolean(PROP_LATEST, true);
        boolean snapshot = getConfiguration().getPreferences().getBoolean(PROP_SNAPSHOT, false);
        Build bld = model.getProject().getBuild();
        if (bld != null) {
            checkPluginList(bld.getPlugins(), model, toRet, release, latest, snapshot);
            PluginManagement pm = bld.getPluginManagement();
            if (pm != null) {
                checkPluginList(pm.getPlugins(), model, toRet, release, latest, snapshot);
            }
        }
        List<Profile> profiles = model.getProject().getProfiles();
        if (profiles != null) {
            for (Profile prof : profiles) {
                BuildBase base = prof.getBuildBase();
                if (base != null) {
                    checkPluginList(base.getPlugins(), model, toRet, release, latest, snapshot);
                    PluginManagement pm = base.getPluginManagement();
                    if (pm != null) {
                        checkPluginList(pm.getPlugins(), model, toRet, release, latest, snapshot);
                    }
                }
            }
        }
        return toRet;
    }

//    private static class ReleaseFix implements Fix {
//        private Plugin plugin;
//
//        ReleaseFix(Plugin plg) {

//            plugin = plg;
//        }
//
//        public String getText() {
//            return "Change to specific version";
//        }
//
//        public ChangeInfo implement() throws Exception {
//            ChangeInfo info = new ChangeInfo();
//            POMModel mdl = plugin.getModel();
//            if (!mdl.getState().equals(Model.State.VALID)) {
//                return info;
//            }
//            mdl.startTransaction();
//            try {
//                plugin.setVersion("XXX");
//            } finally {
//                mdl.endTransaction();
//            }
//            return info;
//        }
//
//    }

    private void checkPluginList(List<Plugin> plugins, POMModel model, List<ErrorDescription> toRet, boolean release, boolean latest, boolean snapshot) {
        if (plugins != null) {
            for (Plugin plg : plugins) {
                String ver = plg.getVersion();
                if (ver != null && ((release && "RELEASE".equals(ver)) ||  //NOI18N
                        (latest &&"LATEST".equals(ver)) || //NOI18N
                        (snapshot && ver.endsWith("SNAPSHOT")) //NOI18N
                    )) {
                    int position = plg.findChildElementPosition(model.getPOMQNames().VERSION.getQName());
                    Line line = NbEditorUtilities.getLine(model.getBaseDocument(), position, false);
                    toRet.add(ErrorDescriptionFactory.createErrorDescription(
                                   configuration.getSeverity(configuration.getPreferences()).toEditorSeverity(),
                            NbBundle.getMessage(ReleaseVersionError.class, "DESC_RELEASE_VERSION"),
                            Collections.<Fix>emptyList(), //Collections.<Fix>singletonList(new ReleaseFix(plg)),
                            model.getBaseDocument(), line.getLineNumber() + 1));
                }
            }
        }
    }

    @Override
    public JComponent getCustomizer(Preferences preferences) {
        if (component == null) {
            component = new ReleaseVersionErrorCustomizer(preferences);
        }
        return component;
    }

    @Override
    public String getSavedValue(JComponent customCustomizer, String key) {
        return ((ReleaseVersionErrorCustomizer) customCustomizer).getSavedValue(key);
    }

    @Override
    public void cancel() {
        component = null;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

}
