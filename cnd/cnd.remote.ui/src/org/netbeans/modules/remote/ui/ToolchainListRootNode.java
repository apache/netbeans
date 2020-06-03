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

package org.netbeans.modules.remote.ui;

import java.awt.Image;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class ToolchainListRootNode extends AbstractNode {

    private final ExecutionEnvironment env;

    public ToolchainListRootNode(ExecutionEnvironment execEnv) {
        super(Children.create(new ToolchainListChildren(execEnv), true), Lookups.singleton(execEnv));
        this.env = execEnv;
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/tools_collection.gif"); // NOI18N
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }


    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "LBL_ToolchainRootNode");
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
                new AddToolchainAction(env),
                new RestoreToolchainsAction(env),
                new ShowToolchainsAction(env, null) };
    }

    /*package*/ static class ToolchainListChildren extends ChildFactory<CompilerSet> implements ChangeListener {

        private final ExecutionEnvironment env;

        public ToolchainListChildren(ExecutionEnvironment env) {
            this.env = env;
            ToolsCacheManager.addChangeListener(WeakListeners.change(this, null));
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

        @Override
        protected boolean createKeys(List<CompilerSet> toPopulate) {
            toPopulate.addAll(CompilerSetManager.get(env).getCompilerSets());
            return true;
        }

        @Override
        protected Node createNodeForKey(CompilerSet key) {
            return new ToolchainNode(env, key);
        }
    }

    private static class ToolchainNode extends AbstractNode {
        private final ExecutionEnvironment env;
        private final CompilerSet compilerSet;

        public ToolchainNode(ExecutionEnvironment env, CompilerSet compilerSet) {
            super(Children.create(new ToolChildren(env, compilerSet), true), Lookups.fixed(env, compilerSet));
            this.env = env;
            this.compilerSet = compilerSet;
        }

        @Override
        public String getHtmlDisplayName() {
            String displayName = getDisplayName();
            if (CompilerSetManager.get(env).isDefaultCompilerSet(compilerSet)) {
                displayName = "<b>" + displayName + "</b>"; // NOI18N
            }
            return displayName;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(ToolchainListRootNode.class, "Toolchain_Name_Text", compilerSet.getName(), compilerSet.getDisplayName()); // NOI18N
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/tools_collection.gif"); // NOI18N
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public Action[] getActions(boolean context) {
            if (compilerSet.getName().equals(CompilerSet.None)) {
                return new Action[]{new ShowToolchainsAction(env, compilerSet)};
            } else {
                return new Action[]{
                        new ShowToolchainsAction(env, compilerSet),
                        new SetDefaultToolchainAction(env, compilerSet),
                        new RemoveToolchainAction(env, compilerSet)};
            }
        }
    }

    private static class ToolChildren extends ChildFactory<Tool> {

        private final ExecutionEnvironment env;
        private final CompilerSet compilerSet;

        public ToolChildren(ExecutionEnvironment env, CompilerSet compilerSet) {
            this.env = env;
            this.compilerSet = compilerSet;
        }

        @Override
        protected boolean createKeys(List<Tool> toPopulate) {
            if (!compilerSet.getName().equals(CompilerSet.None)) {
                for (Tool tool : compilerSet.getTools()) {
                    if (!tool.getPath().isEmpty()) {
                        toPopulate.add(tool);
                    }
                }
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(Tool key) {
            return new ToolNode(env, key);
        }
    }

    private static class ToolNode extends AbstractNode {

        private final ExecutionEnvironment env;
        private final Tool tool;

        public ToolNode(ExecutionEnvironment env, Tool tool) {
            super(Children.LEAF, Lookups.fixed(env, tool));
            this.env = env;
            this.tool = tool;
        }

        @Override
        public String getDisplayName() {
            return tool.getDisplayName() + '[' + tool.getPath() + ']'; //NOI18N
        }

        @Override
        public String getHtmlDisplayName() {
            return tool.getDisplayName() + "  <font color='!textInactiveText'> [" + tool.getPath() + ']'; //NOI18N
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/tool.png"); // NOI18N
        }

        @Override
        public String getShortDescription() {
            return tool.getPath();
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }
    }

}
