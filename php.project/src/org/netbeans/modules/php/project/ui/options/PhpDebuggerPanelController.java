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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
    displayName="#LBL_DebuggerOptions",
//    toolTip="#LBL_DebuggerOptionsTooltip",
    id=PhpDebuggerPanelController.ID,
    location=UiUtils.OPTIONS_PATH,
    position=130
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
    }
    
    @Override
    protected boolean areOptionsChanged() {
        if(parseInteger(debuggerPanel.getPort()) == null || parseInteger(debuggerPanel.getMaxDataLength()) == null
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
