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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion;

import java.awt.EventQueue;
import java.io.File;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.wcadmin.UpgradeAction;
import org.openide.util.actions.SystemAction;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * Keeps some information about working copies as:
 * 1) working copy is incompatible with the current client
 *
 * @author Ondrej Vrabec
 */
public final class WorkingCopyAttributesCache {
    private static WorkingCopyAttributesCache instance;
    
    private final HashSet<String> unsupportedWorkingCopies;
    private final HashSet<String> tooOldClientForWorkingCopies;
    private final HashSet<String> tooOldWorkingCopies;
    private final HashSet<String> askedToUpgradeWorkingCopies;

    /**
     * Returns (and creates if needed) an instance.
     * @return an instance of this class
     */
    public static WorkingCopyAttributesCache getInstance () {
        if (instance == null) {
            instance = new WorkingCopyAttributesCache();
            instance.init();
        }
        return instance;
    }

    private WorkingCopyAttributesCache () {
        unsupportedWorkingCopies = new HashSet<String>(5);
        tooOldClientForWorkingCopies = new HashSet<String>(5);
        tooOldWorkingCopies = new HashSet<String>(5);
        askedToUpgradeWorkingCopies = new HashSet<String>(5);
    }

    private void init () {

    }

    public void logSuppressed (SVNClientException ex, File file) throws SVNClientException {
        if (SvnClientExceptionHandler.isTooOldClientForWC(ex.getMessage())) {
            logUnsupportedWC(ex, file);
        } else if (SvnClientExceptionHandler.isPartOfNewerWC(ex.getMessage())) {
            logTooOldClient(ex, file);
        } else if (SvnClientExceptionHandler.isTooOldWorkingCopy(ex.getMessage())) {
            logTooOldWC(ex, file);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean isSuppressed (SVNClientException ex) {
        boolean retval = false;
        if (SvnClientExceptionHandler.isTooOldClientForWC(ex.getMessage())) {
            retval = true;
        } else if (SvnClientExceptionHandler.isPartOfNewerWC(ex.getMessage())) {
            retval = true;
        } else if (SvnClientExceptionHandler.isTooOldWorkingCopy(ex.getMessage())) {
            retval = true;
        }
        return retval;
    }

    /**
     * Logs the unsupported working copy exception if the file's working copy has not been stored yet.
     * The file's topmost managed parent is saved so next time the exception is not logged again.
     * The exception is thrown again.
     * @param ex
     * @param file
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException
     */
    private void logUnsupportedWC(final SVNClientException ex, File file) throws SVNClientException {
        logWC(ex, file, unsupportedWorkingCopies);
    }

    /**
     * Logs the too old client for this WC exception if the file's working copy has not been stored yet.
     * The file's topmost managed parent is saved so next time the exception is not logged again.
     * The exception is thrown again.
     * @param ex
     * @param file
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException
     */
    private void logTooOldClient (final SVNClientException ex, File file) throws SVNClientException {
        logWC(ex, file, tooOldClientForWorkingCopies);
    }

    private void logTooOldWC (final SVNClientException ex, File file) throws SVNClientException {
        logWC(ex, file, tooOldWorkingCopies);
    }

    private boolean isLogged (String fileName, HashSet<String> loggedWCs) {
        synchronized (loggedWCs) {
            for (String unsupported : loggedWCs) {
                if (fileName.startsWith(unsupported)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void logWC (final SVNClientException ex, File file, HashSet<String> loggedWCs) throws SVNClientException {
        String fileName = file.getAbsolutePath();
        if (!isLogged(fileName, loggedWCs)) {
            final File topManaged = Subversion.getInstance().getTopmostManagedAncestor(file);
            synchronized (loggedWCs) {
                loggedWCs.add(topManaged.getAbsolutePath());
            }
            Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        // this exception may be handled really early after the IDE starts, wait till the projects open at least
                        OpenProjects.getDefault().openProjects().get();
                    } catch (InterruptedException ex) {
                        //
                    } catch (ExecutionException ex) {
                        //
                    }
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            String msg = ex.getMessage().toLowerCase();
                            if (SvnClientExceptionHandler.isTooOldWorkingCopy(msg) 
                                    && (msg.contains("svn upgrade") //NOI18N
                                    || msg.contains("working copy format of ") && msg.contains("is too old") //NOI18N
                                    || msg.contains("needs to be upgraded"))) { //NOI18N
                                SvnClientExceptionHandler.notifyException(ex, false, false);
                                SystemAction.get(UpgradeAction.class).upgrade(topManaged);
                            } else {
                                SvnClientExceptionHandler.notifyException(ex, true, true);
                            }
                        }
                    });
                }
            });
        }
        throw ex;
    }

    /**
     * Returns true if the topmost root for the given file is logged just right now and has not been already logged before, false if has been already logged.
     */
    public boolean logAskedToUpgrade (File file) {
        if (!isLogged(file.getAbsolutePath(), askedToUpgradeWorkingCopies)) {
            final File topManaged = Subversion.getInstance().getTopmostManagedAncestor(file);
            synchronized (askedToUpgradeWorkingCopies) {
                askedToUpgradeWorkingCopies.add(topManaged.getAbsolutePath());
            }
            return true;
        } else {
            return false;
        }
    }
}
