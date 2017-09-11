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

package org.netbeans.modules.subversion.ui.export;

import java.io.File;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.CheckoutCompleted;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 * @author Tomas Stupka
 */
public class ExportAction extends ContextAction {
    
    public ExportAction() { }

    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Export";    // NOI18N
    }

    @Override
    protected int getFileEnabledStatus() {
        return    FileInformation.STATUS_MANAGED
               & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return    FileInformation.STATUS_MANAGED 
               & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
               & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    @Override
    protected boolean enable(Node[] nodes) {
        return nodes != null && nodes.length == 1 && isCacheReady() && getCachedContext(nodes).getRoots().size() > 0;
    }   

    @Override    
    protected void performContextAction(final Node[] nodes) {
        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
        Context ctx = getContext(nodes);

        final File[] roots = SvnUtils.getActionRoots(ctx);
        if(roots == null || roots.length != 1) return;
        File[] files = Subversion.getInstance().getStatusCache().listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);       
        
        File fromFile = roots[0];

        final RequestProcessor rp = createRequestProcessor(ctx);
        final boolean hasChanges = files.length > 0;
        final Export export = new Export(fromFile, hasChanges);
        if(export.showDialog()) {
            performExport(export, rp, nodes, roots);
        }
    }

    private void performExport(final Export export, final RequestProcessor rp, final Node[] nodes, final File[] roots) {
        rp.post(new Runnable() {
            @Override
            public void run() {
                ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(ExportAction.this, nodes) {
                    @Override
                    public void perform() {
                        File fromFile = export.getFromFile();
                        File toFile = export.getToFile();
                        toFile.mkdir();
                        if(isCanceled()) {
                            return;
                        }

                        SvnClient client;
                        try {
                            client = Subversion.getInstance().getClient(fromFile);
                            client.doExport (fromFile, toFile, true);
                        } catch (SVNClientException ex) {
                            SvnClientExceptionHandler.notifyException(ex, true, true); // should not happen
                            return;
                        }
                        if(export.getScanAfterExport()) {
                            CheckoutCompleted cc = new CheckoutCompleted(toFile, new String[] {"."});
                            if (isCanceled()) {
                                return;
                            }
                            cc.scanForProjects(this, CheckoutCompleted.Type.EXPORT);
                        }
                    }
                };
                support.start(rp);
            }
        });
    }

}
