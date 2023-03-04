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
package org.netbeans.modules.mercurial.ui.view;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;

import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * View action for mercurial: 
 * hg view - launch hg view to view the dependency tree for the repository
 * 
 * @author John Rice
 */
public class ViewAction extends ContextAction {
    
    private static final String HG_SCRIPTS_DIR = "scripts";

    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_View";                                     //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        final File roots[] = HgUtils.getActionRoots(context);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);

        String repository = root.getAbsolutePath();
        RequestProcessor rp = Mercurial.getInstance().getParallelRequestProcessor();
        rp.post(new Runnable() {
            public void run() {
                performView(root);
            }
        });
    }

    static void performView(File root) {
        OutputLogger logger = OutputLogger.getLogger(root);
        try {
            logger.outputInRed(NbBundle.getMessage(ViewAction.class, "MSG_VIEW_TITLE")); // NOI18N
            logger.outputInRed(NbBundle.getMessage(ViewAction.class, "MSG_VIEW_TITLE_SEP")); // NOI18N

            String hgkCommand = HgCommand.HGK_COMMAND;
            if(Utilities.isWindows()){ 
                hgkCommand = hgkCommand + HgCommand.HG_WINDOWS_CMD;
            }
            boolean bHgkFound = false;
            if(HgUtils.isInUserPath(hgkCommand)){
                    bHgkFound = true;                
            } else if(HgUtils.isSolaris()){
                File f = new File(HgCommand.HG_HGK_PATH_SOLARIS10, hgkCommand);
                if(f.exists() && f.isFile()) 
                    bHgkFound = true;
            }else if(Utilities.isWindows()){
                bHgkFound = HgUtils.isInUserPath(HG_SCRIPTS_DIR + File.separator + hgkCommand);                    
            }
            boolean bHgkPropExists = HgConfigFiles.getSysInstance().containsProperty(
                            HgConfigFiles.HG_EXTENSIONS, HgConfigFiles.HG_EXTENSIONS_HGK);
            
            if(!bHgkFound){
                logger.outputInRed(
                            NbBundle.getMessage(ViewAction.class, "MSG_VIEW_HGK_NOT_FOUND_INFO")); // NOI18N
                logger.output(""); // NOI18N
                logger.outputInRed(NbBundle.getMessage(ViewAction.class, "MSG_VIEW_HGK_NOT_FOUND"));    // NOI18N
                logger.outputInRed(NbBundle.getMessage(ViewAction.class, "MSG_VIEW_HGK_NOT_FOUND_TITLE"));  // NOI18N
            }
            if(!bHgkPropExists){
                boolean bConfirmSetHgkProp = false;
                bConfirmSetHgkProp = HgUtils.confirmDialog(
                        ViewAction.class, "MSG_VIEW_SETHGK_PROP_CONFIRM_TITLE", // NOI18N
                        "MSG_VIEW_SETHGK_PROP_CONFIRM_QUERY"); // NOI18N                
                if (bConfirmSetHgkProp) {
                    logger.outputInRed(
                            NbBundle.getMessage(ViewAction.class, "MSG_VIEW_SETHGK_PROP_DO_INFO")); // NOI18N
                    HgConfigFiles hcf = HgConfigFiles.getSysInstance();
                    if (hcf.getException() == null) {
                        hcf.setProperty(HgConfigFiles.HG_EXTENSIONS_HGK, ""); // NOI18N
                    } else {
                        Mercurial.LOG.log(Level.WARNING, ViewAction.class.getName() + ": Cannot set hgk property"); // NOI18N
                        Mercurial.LOG.log(Level.INFO, null, hcf.getException());
                        HgModuleConfig.notifyParsingError();
                    }
                } else {
                    logger.outputInRed(
                            NbBundle.getMessage(ViewAction.class, "MSG_VIEW_NOTSETHGK_PROP_INFO")); // NOI18N
                    logger.output(""); // NOI18N
                    logger.closeLog();
                    return;
                }
            }
            
            logger.outputInRed(NbBundle.getMessage(ViewAction.class, 
                    "MSG_VIEW_LAUNCH_INFO", root.getAbsolutePath())); // NOI18N
            logger.output(""); // NOI18N
            HgCommand.doView(root, logger);
        } catch (HgException.HgCommandCanceledException ex) {
            // canceled by user, do nothing
        } catch (HgException ex) {
            HgUtils.notifyException(ex);
        } finally {
            logger.closeLog();
        }
    }
}
