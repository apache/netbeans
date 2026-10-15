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

package org.netbeans.modules.hudson.ui.nodes;

import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.api.Utilities;
import org.netbeans.modules.hudson.ui.api.UI;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.netbeans.modules.hudson.api.ui.OpenableInBrowser;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

class HudsonJobBuildNode extends AbstractNode {

    private final HudsonJobBuild build;
    private String htmlDisplayName;

    public HudsonJobBuildNode(HudsonJobBuild build) {
        super(makeChildren(build), Lookups.singleton(build));
        setName(Integer.toString(build.getNumber()));
        setDisplayName(NbBundle.getMessage(HudsonJobBuildNode.class, "HudsonJobBuildNode.displayName", build.getNumber()));
        Color effectiveColor;
        if (build.isBuilding()) {
            effectiveColor = build.getJob().getColor();
        } else {
            effectiveColor = Utilities.getColorForBuild(build);
        }
        try {
            htmlDisplayName = effectiveColor.colorizeDisplayName(XMLUtil.toElementContent(getDisplayName()));
        } catch (CharConversionException x) {
            htmlDisplayName = null;
        }
        setIconBaseWithExtension(effectiveColor.iconBase());
        this.build = build;
    }

    public @Override String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    public @Override Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(UI.showChangesAction(build));
        actions.add(UI.showConsoleAction(build));
        actions.add(UI.showFailuresAction());
        actions.add(null);
        if (build instanceof OpenableInBrowser) {
            actions.add(OpenUrlAction.forOpenable((OpenableInBrowser) build));
        }
        return actions.toArray(new Action[0]);
    }

    private static Children makeChildren(final HudsonJobBuild build) {
        return Children.create(new ChildFactory<Object>() {
            final Object ARTIFACTS = new Object();
            protected boolean createKeys(List<Object> toPopulate) {
                Collection<? extends HudsonMavenModuleBuild> modules = build.getMavenModules();
                if (modules.isEmpty()) {
                    // XXX is it possible to cheaply check in advance if the build has any artifacts?
                    if (build.getArtifacts() != null) {
                        toPopulate.add(ARTIFACTS);
                    }
                } else {
                    toPopulate.addAll(modules);
                }
                return true;
            }
            protected @Override Node createNodeForKey(Object key) {
                if (key instanceof HudsonMavenModuleBuild) {
                    return new HudsonMavenModuleBuildNode((HudsonMavenModuleBuild) key);
                } else {
                    assert key == ARTIFACTS : key;
                    return new HudsonArtifactsNode(build);
                }
            }
        }, false);
    }

}
