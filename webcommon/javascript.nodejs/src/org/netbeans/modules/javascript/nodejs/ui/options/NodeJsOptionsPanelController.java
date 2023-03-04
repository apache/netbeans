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
package org.netbeans.modules.javascript.nodejs.ui.options;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptions;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptionsValidator;
import org.netbeans.modules.javascript.v8debug.api.DebuggerOptions;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


@NbBundle.Messages("NodeJsOptionsPanelController.name=Node.js")
@OptionsPanelController.SubRegistration(
    location = NodeJsOptionsPanelController.OPTIONS_CATEGORY,
    id = NodeJsOptionsPanelController.OPTIONS_SUBCATEGORY,
    displayName = "#NodeJsOptionsPanelController.name" // NOI18N
)
public final class NodeJsOptionsPanelController extends OptionsPanelController implements ChangeListener {

    public static final String OPTIONS_CATEGORY = "Html5"; // NOI18N
    public static final String OPTIONS_SUBCATEGORY = "NodeJs"; // NOI18N
    public static final String OPTIONS_PATH = OPTIONS_CATEGORY + "/" + OPTIONS_SUBCATEGORY; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    // @GuardedBy("EDT")
    private NodeJsOptionsPanel nodeJsOptionsPanel;
    private volatile boolean changed = false;
    private boolean firstOpening = true;


    @Override
    public void update() {
        assert EventQueue.isDispatchThread();
        if (firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            getPanel().setNode(getNodeJsOptions().getNode());
            getPanel().setNodeSources(getNodeJsOptions().getNodeSources());
            getPanel().setStopAtFirstLine(getDebuggerOptions().isBreakAtFirstLine());
            getPanel().setLiveEdit(getDebuggerOptions().isLiveEdit());
            getPanel().setNpm(getNodeJsOptions().getNpm());
            getPanel().setNpmIgnoreNodeModules(getNodeJsOptions().isNpmIgnoreNodeModules());
            getPanel().setExpress(getNodeJsOptions().getExpress());
        }
        changed = false;
    }

    @Override
    public void applyChanges() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                getNodeJsOptions().setNode(getPanel().getNode());
                getNodeJsOptions().setNodeSources(getPanel().getNodeSources());
                getDebuggerOptions().setBreakAtFirstLine(getPanel().isStopAtFirstLine());
                getDebuggerOptions().setLiveEdit(getPanel().isLiveEdit());
                getNodeJsOptions().setNpm(getPanel().getNpm());
                getNodeJsOptions().setNpmIgnoreNodeModules(getPanel().isNpmIgnoreNodeModules());
                getNodeJsOptions().setExpress(getPanel().getExpress());
                changed = false;
            }
        });
    }

    @Override
    public void cancel() {
        if (isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            getPanel().setNode(getNodeJsOptions().getNode());
            getPanel().setNodeSources(getNodeJsOptions().getNodeSources());
            getPanel().setStopAtFirstLine(getDebuggerOptions().isBreakAtFirstLine());
            getPanel().setLiveEdit(getDebuggerOptions().isLiveEdit());
            getPanel().setNpm(getNodeJsOptions().getNpm());
            getPanel().setNpmIgnoreNodeModules(getNodeJsOptions().isNpmIgnoreNodeModules());
            getPanel().setExpress(getNodeJsOptions().getExpress());
        }
    }

    @Override
    public boolean isValid() {
        assert EventQueue.isDispatchThread();
        NodeJsOptionsPanel panel = getPanel();
        ValidationResult result = new NodeJsOptionsValidator()
                .validateNode(panel.getNode(), panel.getNodeSources())
                .validateNpm(panel.getNpm())
                .validateExpress(panel.getExpress())
                .getResult();
        // errors
        if (result.hasErrors()) {
            panel.setError(result.getFirstErrorMessage());
            return false;
        }
        // warnings
        if (result.hasWarnings()) {
            panel.setWarning(result.getFirstWarningMessage());
            return true;
        }
        // everything ok
        panel.setError(" "); // NOI18N
        return true;
    }

    @Override
    public boolean isChanged() {
        String saved = getNodeJsOptions().getNode();
        String current = getPanel().getNode().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = getNodeJsOptions().getNodeSources();
        current = getPanel().getNodeSources();
        if (saved == null ? current != null : !saved.equals(current)) {
            return true;
        }
        if (getDebuggerOptions().isBreakAtFirstLine() != getPanel().isStopAtFirstLine()) {
            return true;
        }
        if (getDebuggerOptions().isLiveEdit() != getPanel().isLiveEdit()) {
            return true;
        }
        saved = getNodeJsOptions().getNpm();
        current = getPanel().getNpm().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        if (getNodeJsOptions().isNpmIgnoreNodeModules() != getPanel().isNpmIgnoreNodeModules()) {
            return true;
        }
        saved = getNodeJsOptions().getExpress();
        current = getPanel().getExpress().trim();
        return saved == null ? !current.isEmpty() : !saved.equals(current);
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        assert EventQueue.isDispatchThread();
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.javascript.nodejs.ui.options.NodeJsOptionsPanel"); // NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (!changed) {
            changed = true;
            propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private NodeJsOptionsPanel getPanel() {
        assert EventQueue.isDispatchThread();
        if (nodeJsOptionsPanel == null) {
            nodeJsOptionsPanel = NodeJsOptionsPanel.create();
            nodeJsOptionsPanel.addChangeListener(this);
        }
        return nodeJsOptionsPanel;
    }

    private NodeJsOptions getNodeJsOptions() {
        return NodeJsOptions.getInstance();
    }

    private DebuggerOptions getDebuggerOptions() {
        return DebuggerOptions.getInstance();
    }

}
