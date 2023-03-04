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
package org.netbeans.modules.php.project.ui.options;

import javax.swing.JComponent;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Controller for {@link PhpDebuggerPanel}.
 */
@OptionsPanelController.SubRegistration(
        displayName = "#LBL_DebuggerOptions",
        //    toolTip="#LBL_DebuggerOptionsTooltip",
        id = PhpDebuggerPanelController.ID,
        location = UiUtils.OPTIONS_PATH,
        position = 130
)
public class PhpDebuggerPanelController extends BaseOptionsPanelController {

    public static final String ID = "Debugger"; // NOI18N

    private PhpDebuggerPanel debuggerPanel = null;

    @Override
    public void updateInternal() {
        debuggerPanel.setPort(getPhpOptions().getDebuggerPort());
        debuggerPanel.setSessionId(getPhpOptions().getDebuggerSessionId());
        debuggerPanel.setMaxDataLength(getPhpOptions().getDebuggerMaxDataLength());
        debuggerPanel.setStoppedAtTheFirstLine(getPhpOptions().isDebuggerStoppedAtTheFirstLine());
        debuggerPanel.setWatchesAndEval(getPhpOptions().isDebuggerWatchesAndEval());
        debuggerPanel.setMaxStructuresDepth(getPhpOptions().getDebuggerMaxStructuresDepth());
        debuggerPanel.setMaxChildren(getPhpOptions().getDebuggerMaxChildren());
        debuggerPanel.setShowUrls(getPhpOptions().isDebuggerShowUrls());
        debuggerPanel.setShowConsole(getPhpOptions().isDebuggerShowConsole());
        debuggerPanel.setResolveBreakpoints(getPhpOptions().isDebuggerResolveBreakpoints());
    }

    @Override
    public void applyChangesInternal() {
        getPhpOptions().setDebuggerPort(parseInteger(debuggerPanel.getPort()));
        getPhpOptions().setDebuggerSessionId(debuggerPanel.getSessionId());
        getPhpOptions().setDebuggerMaxDataLength(parseInteger(debuggerPanel.getMaxDataLength()));
        getPhpOptions().setDebuggerStoppedAtTheFirstLine(debuggerPanel.isStoppedAtTheFirstLine());
        getPhpOptions().setDebuggerWatchesAndEval(debuggerPanel.isWatchesAndEval());
        getPhpOptions().setDebuggerMaxStructuresDepth(parseInteger(debuggerPanel.getMaxStructuresDepth()));
        getPhpOptions().setDebuggerMaxChildren(parseInteger(debuggerPanel.getMaxChildren()));
        getPhpOptions().setDebuggerShowUrls(debuggerPanel.isShowUrls());
        getPhpOptions().setDebuggerShowConsole(debuggerPanel.isShowConsole());
        getPhpOptions().setDebuggerResolveBreakpoints(debuggerPanel.isResolveBreakpoints());
    }

    @Override
    protected boolean areOptionsChanged() {
        if (parseInteger(debuggerPanel.getPort()) == null || parseInteger(debuggerPanel.getMaxDataLength()) == null
                || parseInteger(debuggerPanel.getMaxStructuresDepth()) == null || parseInteger(debuggerPanel.getMaxChildren()) == null) {
            return false;
        }
        return getPhpOptions().getDebuggerPort() != parseInteger(debuggerPanel.getPort())
                || !getPhpOptions().getDebuggerSessionId().equals(debuggerPanel.getSessionId())
                || getPhpOptions().getDebuggerMaxDataLength() != parseInteger(debuggerPanel.getMaxDataLength())
                || getPhpOptions().isDebuggerStoppedAtTheFirstLine() != debuggerPanel.isStoppedAtTheFirstLine()
                || getPhpOptions().getDebuggerMaxStructuresDepth() != parseInteger(debuggerPanel.getMaxStructuresDepth())
                || getPhpOptions().getDebuggerMaxChildren() != parseInteger(debuggerPanel.getMaxChildren())
                || getPhpOptions().isDebuggerShowUrls() != debuggerPanel.isShowUrls()
                || getPhpOptions().isDebuggerShowConsole() != debuggerPanel.isShowConsole()
                || getPhpOptions().isDebuggerResolveBreakpoints() != debuggerPanel.isResolveBreakpoints()
                || getPhpOptions().isDebuggerWatchesAndEval() != debuggerPanel.isWatchesAndEval();
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (debuggerPanel == null) {
            debuggerPanel = new PhpDebuggerPanel();
            debuggerPanel.addChangeListener(this);
        }
        return debuggerPanel;
    }

    @Override
    protected boolean validateComponent() {
        // errors
        Integer port = parseInteger(debuggerPanel.getPort());
        if (port == null || port < 1) {
            debuggerPanel.setError(NbBundle.getMessage(PhpDebuggerPanelController.class, "MSG_DebuggerInvalidPort"));
            return false;
        }
        String sessionId = debuggerPanel.getSessionId();
        if (sessionId == null
                || sessionId.trim().length() == 0
                || sessionId.contains(" ")) { // NOI18N
            debuggerPanel.setError(NbBundle.getMessage(PhpDebuggerPanelController.class, "MSG_DebuggerInvalidSessionId"));
            return false;
        }
        Integer maxDataLength = parseInteger(debuggerPanel.getMaxDataLength());
        if (maxDataLength == null
                || maxDataLength == 0
                || maxDataLength < -1) {
            debuggerPanel.setError(NbBundle.getMessage(PhpDebuggerPanelController.class, "MSG_DebuggerInvalidMaxDataLength"));
            return false;
        }
        Integer maxStructuresDepth = parseInteger(debuggerPanel.getMaxStructuresDepth());
        if (maxStructuresDepth == null || maxStructuresDepth < 1) {
            debuggerPanel.setError(NbBundle.getMessage(PhpDebuggerPanelController.class, "MSG_DebuggerInvalidMaxStructuresDepth"));
            return false;
        }
        Integer maxChildren = parseInteger(debuggerPanel.getMaxChildren());
        if (maxChildren == null || maxChildren < 1) {
            debuggerPanel.setError(NbBundle.getMessage(PhpDebuggerPanelController.class, "MSG_DebuggerInvalidMaxChildren"));
            return false;
        }

        // everything ok
        debuggerPanel.setError(" "); // NOI18N
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.project.ui.options.PhpDebuggerPanelController"); // NOI18N
    }

}
