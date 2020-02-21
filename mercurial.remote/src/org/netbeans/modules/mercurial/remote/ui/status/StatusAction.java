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
package org.netbeans.modules.mercurial.remote.ui.status;

import java.util.Calendar;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.remote.FileStatusCache;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.nodes.Node;

/**
 * Status action for mercurial: 
 * hg status - show changed files in the working directory
 * 
 * 
 */
public class StatusAction extends ContextAction {
    
    private static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/remote/resources/icons/show_changes.png"; //NOI18N

    public StatusAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    public boolean enable (Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        return HgUtils.isFromHgRepository(context);
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_ShowChanges"; // NOI18N
    }

    @Override
    public void performContextAction (Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);

        VCSFileProxy [] files = context.getRootFiles().toArray(new VCSFileProxy[context.getRootFiles().size()]);
        if (files == null || files.length == 0) {
            return;
        }
                
        final HgVersioningTopComponent stc = HgVersioningTopComponent.findInstance();
        stc.setContentTitle(VCSFileProxySupport.getContextDisplayName(context)); 
        stc.setContext(context);
        stc.open(); 
        stc.requestActive();
        stc.performRefreshAction();
    }

    /**
     * Connects to repository and gets recent status.
     */
    public static void executeStatus(final VCSContext context, HgProgressSupport support) {
        if (context == null || context.getRootFiles().isEmpty()) {
            return;
        }

        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        Calendar start = Calendar.getInstance();
        cache.refreshAllRoots(context.getRootFiles());
        Calendar end = Calendar.getInstance();
        Mercurial.STATUS_LOG.log(Level.FINE, "executeStatus: refreshCached took {0} millisecs", end.getTimeInMillis() - start.getTimeInMillis()); // NOI18N
    }
}
