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
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.ui.api.UI;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.netbeans.modules.hudson.api.ui.OpenableInBrowser;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 * Represents one Maven module in a build.
 */
class HudsonMavenModuleBuildNode extends AbstractNode {

    private final HudsonMavenModuleBuild module;
    private String htmlDisplayName;

    public HudsonMavenModuleBuildNode(HudsonMavenModuleBuild module) {
        super(makeChildren(module), Lookups.singleton(module));
        this.module = module;
        setName(module.getName());
        setDisplayName(module.getDisplayName());
        try {
            htmlDisplayName = module.getColor().colorizeDisplayName(XMLUtil.toElementContent(getDisplayName()));
        } catch (CharConversionException x) {
            htmlDisplayName = null;
        }
        setIconBaseWithExtension(module.getColor().iconBase());
    }

    public @Override String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    public @Override Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(UI.showConsoleAction(module));
        actions.add(UI.showFailuresAction());
        actions.add(null);
        if (module instanceof OpenableInBrowser) {
            actions.add(OpenUrlAction.forOpenable((OpenableInBrowser) module));
        }
        return actions.toArray(new Action[0]);
    }

    private static Children makeChildren(final HudsonMavenModuleBuild module) {
        return Children.create(new ChildFactory<Object>() {
            final Object ARTIFACTS = new Object();
            protected boolean createKeys(List<Object> toPopulate) {
                // XXX is it possible to cheaply check in advance if the build has any artifacts?
                if (module.getArtifacts() != null) {
                    toPopulate.add(ARTIFACTS);
                }
                return true;
            }
            protected @Override Node createNodeForKey(Object key) {
                assert key == ARTIFACTS : key;
                return new HudsonArtifactsNode(module);
            }
        }, false);
    }

}
