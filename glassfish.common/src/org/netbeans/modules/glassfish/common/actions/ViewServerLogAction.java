/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.glassfish.common.actions;

import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;
import org.netbeans.modules.glassfish.common.CommonServerSupport;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.LogViewMgr;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;

/** 
 * This action will open or focus the server log window for the selected server
 * instance.
 * 
 * @author Peter Williams
 */
public class ViewServerLogAction extends NodeAction {

    private static final String SHOW_SERVER_LOG_ICONBASE =
            "org/netbeans/modules/glassfish/common/resources/serverlog.gif"; // NOI18N

    @Override
    protected void performAction(Node[] nodes) {
        Lookup lookup = nodes[0].getLookup();
        CommonServerSupport commonSupport = lookup.lookup(CommonServerSupport.class);
        if(commonSupport != null) {
            LogViewMgr.displayOutput(commonSupport.getInstance(), lookup);
        }
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length < 1 || nodes[0] == null) {
            return false;
        }
        GlassfishModule commonSupport = nodes[0].getLookup().lookup(GlassfishModule.class);
        if (commonSupport == null || !(commonSupport.getInstance() instanceof GlassfishInstance)) {
            return false;
        }
        GlassfishInstance server = (GlassfishInstance) commonSupport.getInstance();
        String uri = server.getUrl();
        return uri != null && uri.length() > 0
                && ((!server.isRemote()
                && ServerUtils.getServerLogFile(server).canRead())
                || (commonSupport.isRestfulLogAccessSupported()
                && server.isRemote() && isRunning(commonSupport)));
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ViewServerLogAction.class, "CTL_ViewServerLogAction");
    }

    @Override
    protected String iconResource() {
        return SHOW_SERVER_LOG_ICONBASE;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private boolean isRunning(GlassfishModule commonSupport) {
        ServerState ss = commonSupport.getServerState();
        return ss == ServerState.RUNNING || ss == ServerState.RUNNING_JVM_DEBUG || ss == ServerState.RUNNING_JVM_PROFILER;
    }
}
