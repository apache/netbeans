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
package org.netbeans.modules.cnd.lsp.makeproject.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.lsp.makeproject.ui.actions.ClangdConfigureAction;
import org.netbeans.modules.cnd.lsp.makeproject.ui.actions.ClangdStartAction;
import org.netbeans.modules.cnd.lsp.server.ClangdProcess;
import org.netbeans.modules.cnd.lsp.server.LSPServerState;
import org.netbeans.modules.cnd.lsp.server.LSPServerSupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Represents the running clangd process.
 * @author antonio
 */
public class ClangdLSPServerNode extends AbstractNode implements PropertyChangeListener {

    private final Project project;
    private LSPServerSupport lspServerSupport;

    ClangdLSPServerNode(Project project) {
        this(project, new InstanceContent());
    }

    ClangdLSPServerNode(Project project, InstanceContent instanceContent) {
        super(Children.LEAF, new AbstractLookup(instanceContent));
        this.project = project;
        this.lspServerSupport = project.getLookup().lookup(LSPServerSupport.class);
        this.lspServerSupport.addPropertyChangeListener(this);
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(LSPServersNode.class, "ClangdLSPServerNode.shortDescription"); // NOI18N
    }

    @Override
    public String getDisplayName() {
        String displayName = NbBundle.getMessage(LSPServersNode.class, "ClangdLSPServerNode.displayName"); // NOI18N
        LSPServerState state = ClangdProcess.getInstance().getState();
        return String.format("%s (%s)", displayName, state.getDisplayName()); // NOI18N
    }

    @Override
    public Image getIcon(int type) {
        Image clangd = ImageUtilities.loadImage("org/netbeans/modules/cnd/lsp/makeproject/ui/resources/clangd.llvm.org.png"); // NOI18N
        LSPServerState state = ClangdProcess.getInstance().getState();
        Image stateBadge = state.getBadge();
        return ImageUtilities.mergeImages(clangd, stateBadge, 0, 0);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    private static Action[] ACTIONS = {
        new ClangdStartAction(),
        new ClangdConfigureAction(),};

    @Override
    public Action[] getActions(boolean context) {
        return ACTIONS;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        assert SwingUtilities.isEventDispatchThread();
        if (LSPServerSupport.PROP_CLANGD_PROCESS_STATE.equals(evt.getPropertyName())) {
            // LSP Server changed state, fire a property change for icon and name
            firePropertyChange(PROP_ICON, null, null);
            firePropertyChange(PROP_DISPLAY_NAME, null, null);
        }
    }

    public static final String PROP_COMMANDS = "clangd.commands"; // NOI18N
    public static final String PROP_STATE = "clangd.state"; // NOI18N

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();

        Property clangdStateProperty
                = new PropertySupport.ReadOnly<String>(
                        PROP_STATE,
                        String.class,
                        NbBundle.getMessage(LSPServersNode.class, "ClangdLSPServerNode.state.name"), // NOI18N
                        NbBundle.getMessage(LSPServersNode.class, "ClangdLSPServerNode.state.description") // NOI18N
                ) {
            @Override
            public String getValue()
                    throws IllegalAccessException, InvocationTargetException {
                LSPServerState state = ClangdProcess.getInstance().getState();
                return state.getDisplayName();
            }
        };


        Property clangdCommandsProperty
                = new PropertySupport.ReadOnly<String>(
                        PROP_COMMANDS,
                        String.class,
                        NbBundle.getMessage(LSPServersNode.class, "ClangdLSPServerNode.commands.name"), // NOI18N
                        NbBundle.getMessage(LSPServersNode.class, "ClangdLSPServerNode.commands.description") // NOI18N
                ) {
            @Override
            public String getValue()
                    throws IllegalAccessException, InvocationTargetException {
                String [] commands = ClangdProcess.getInstance().getCommands();
                return String.join(" ", commands);
            }
        };

        set.put(clangdStateProperty);
        set.put(clangdCommandsProperty);
        sheet.put(set);
        return sheet;
    }

}
