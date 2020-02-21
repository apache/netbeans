/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote.client;

import org.netbeans.modules.subversion.remote.api.SVNNotificationHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.netbeans.modules.subversion.remote.api.Depth;
import org.netbeans.modules.subversion.remote.api.ISVNAnnotations;
import org.netbeans.modules.subversion.remote.api.ISVNDirEntry;
import org.netbeans.modules.subversion.remote.api.ISVNDirEntryWithLock;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNLogMessage;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.ISVNProperty;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNDiffSummary;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.cli.commands.BlameCommand;
import org.netbeans.modules.subversion.remote.client.cli.commands.CatCommand;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.Cancellable;

/**
 *
 * 
 */
public interface SvnClient extends Cancellable, SvnClientDescriptor {

    void addDirectory(VCSFileProxy dir, boolean recursive) throws SVNClientException;

    void addFile(VCSFileProxy file) throws SVNClientException;

    void addNotifyListener(ISVNNotifyListener l);

    void addToIgnoredPatterns(VCSFileProxy file, String value) throws SVNClientException;

    //ISVNAnnotations annotate(SVNUrl url, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException;

    ISVNAnnotations annotate(VCSFileProxy file, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException;

    ISVNAnnotations annotate(VCSFileProxy file, BlameCommand blameCmd, CatCommand catCmd) throws SVNClientException;

    void cancelOperation() throws SVNClientException;

    /**
     * @return true if old 1.5 format is supported
     */
    boolean checkSupportedVersion() throws SVNClientException;

    void checkout(SVNUrl url, VCSFileProxy file, SVNRevision revision, boolean recurse) throws SVNClientException;

    void cleanup(VCSFileProxy file) throws SVNClientException;

    long commit(VCSFileProxy[] files, String message, boolean recurse) throws SVNClientException;

    long commit(VCSFileProxy[] files, String message, boolean keep, boolean recursive) throws SVNClientException;

    // unsupported start
    void copy(VCSFileProxy fileFrom, VCSFileProxy fileTo) throws SVNClientException;

    void copy(VCSFileProxy file, SVNUrl url, String msg) throws SVNClientException;

    void copy(SVNUrl url, VCSFileProxy file, SVNRevision rev) throws SVNClientException;

    void copy(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev) throws SVNClientException;

    /**
     * Copies all files from <code>files</code> to repository URL at <code>targetUrl</code>.
     * @param files array of files which will be copied
     * @param targetUrl destination repository Url
     * @param message commit message
     * @param addAsChild not supported
     * @param makeParents creates parent folders
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException
     */
    void copy(VCSFileProxy[] files, SVNUrl targetUrl, String message, boolean addAsChild, boolean makeParents) throws SVNClientException;

    void copy(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev, boolean makeParents) throws SVNClientException;

    SVNDiffSummary[] diffSummarize(SVNUrl arg0, SVNRevision arg1, SVNUrl arg2, SVNRevision arg3, Depth arg4, boolean arg5) throws SVNClientException;

    void dispose();

    void doExport(SVNUrl url, VCSFileProxy destination, SVNRevision revision, boolean force) throws SVNClientException;

    void doExport(VCSFileProxy fileFrom, VCSFileProxy fileTo, boolean force) throws SVNClientException;

    void doImport(VCSFileProxy File, SVNUrl url, String msg, boolean recursivelly) throws SVNClientException;

    InputStream getContent(SVNUrl url, SVNRevision rev) throws SVNClientException;

    InputStream getContent(VCSFileProxy file, SVNRevision rev) throws SVNClientException;

    InputStream getContent(SVNUrl url, SVNRevision rev, SVNRevision pegRevision) throws SVNClientException;

    List<String> getIgnoredPatterns(VCSFileProxy file) throws SVNClientException;

    ISVNInfo getInfo(Context context, SVNUrl url) throws SVNClientException;

    ISVNInfo getInfo(VCSFileProxy file) throws SVNClientException;

    ISVNInfo getInfo(Context context, SVNUrl url, SVNRevision revision, SVNRevision pegging) throws SVNClientException;

    ISVNInfo getInfoFromWorkingCopy(VCSFileProxy file) throws SVNClientException;

    ISVNDirEntry[] getList(SVNUrl url, SVNRevision revision, boolean recursivelly) throws SVNClientException;

    ISVNDirEntryWithLock[] getListWithLocks(SVNUrl svnurl, SVNRevision svnr, SVNRevision svnr1, boolean bln) throws SVNClientException;

    ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException;

    ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revStart, SVNRevision revEnd, boolean fetchChangePath) throws SVNClientException;

    ISVNLogMessage[] getLogMessages(SVNUrl url, String[] paths, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath) throws SVNClientException;

    ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revPeg, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath, long limit) throws SVNClientException;

    ISVNLogMessage[] getLogMessages(VCSFileProxy file, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException;

    ISVNLogMessage[] getLogMessages(VCSFileProxy file, SVNRevision revStart, SVNRevision revEnd, boolean fetchChangePath) throws SVNClientException;

    ISVNLogMessage[] getLogMessages(VCSFileProxy file, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath) throws SVNClientException;

    ISVNLogMessage[] getLogMessages(VCSFileProxy file, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath, long limit) throws SVNClientException;

    ISVNLogMessage[] getLogMessages(VCSFileProxy file, SVNRevision pegRevision, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath, long limit, boolean includeMergedRevisions) throws SVNClientException;

    SVNNotificationHandler getNotificationHandler();

    String getPostCommitError();

    ISVNProperty[] getProperties(final VCSFileProxy file) throws SVNClientException;

    ISVNProperty[] getProperties(SVNUrl url) throws SVNClientException;

    ISVNProperty[] getProperties(SVNUrl url, SVNRevision revision, SVNRevision pegRevision) throws SVNClientException;

    ISVNProperty[] getProperties(SVNUrl url, SVNRevision revision, SVNRevision pegRevision, boolean recursive) throws SVNClientException;

    // parser start
    ISVNStatus getSingleStatus(VCSFileProxy file) throws SVNClientException;

    ISVNStatus[] getStatus(VCSFileProxy[] files) throws SVNClientException;

    ISVNStatus[] getStatus(VCSFileProxy file, boolean descend, boolean getAll, boolean contactServer) throws SVNClientException;

    // XXX merge with get status
    ISVNStatus[] getStatus(VCSFileProxy file, boolean descend, boolean getAll, boolean contactServer, boolean ignoreExternals) throws SVNClientException;

    ISVNStatus[] getStatus(VCSFileProxy file, boolean descend, boolean getAll) throws SVNClientException;

    String getVersion() throws SVNClientException;

    void merge(SVNUrl startUrl, SVNRevision startRev, SVNUrl endUrl, SVNRevision endRev, VCSFileProxy file, boolean force, boolean recurse) throws SVNClientException;

    void merge(SVNUrl startUrl, SVNRevision startRev, SVNUrl endUrl, SVNRevision endRev, VCSFileProxy file, boolean force, boolean recurse, boolean dryRun) throws SVNClientException;

    void merge(SVNUrl startUrl, SVNRevision startRev, SVNUrl endUrl, SVNRevision endRev, VCSFileProxy file, boolean force, boolean recurse, boolean dryRun, boolean ignoreAncestry) throws SVNClientException;

    void mergeReintegrate(SVNUrl arg0, SVNRevision arg1, VCSFileProxy arg2, boolean arg3, boolean arg4) throws SVNClientException;

    void mkdir(SVNUrl url, String msg) throws SVNClientException;

    void mkdir(VCSFileProxy file) throws SVNClientException;

    void move(VCSFileProxy fromFile, VCSFileProxy toFile, boolean force) throws SVNClientException;

    void propertyDel(VCSFileProxy file, String name, boolean rec) throws SVNClientException;

    ISVNProperty propertyGet(final VCSFileProxy file, final String name) throws SVNClientException;

    ISVNProperty propertyGet(SVNUrl url, String name) throws SVNClientException;

    ISVNProperty propertyGet(final SVNUrl url, SVNRevision rev, SVNRevision peg, final String name) throws SVNClientException;

    void propertySet(VCSFileProxy file, String name, String value, boolean rec) throws SVNClientException;

    void propertySet(VCSFileProxy file, String name, VCSFileProxy propFile, boolean rec) throws SVNClientException, IOException;

    void relocate(Context context, String from, String to, String path, boolean rec) throws SVNClientException;

    void remove(VCSFileProxy[] files, boolean force) throws SVNClientException;

    void removeNotifyListener(ISVNNotifyListener l);

    void resolved(VCSFileProxy file) throws SVNClientException;

    void revert(VCSFileProxy file, boolean recursivelly) throws SVNClientException;

    void revert(VCSFileProxy[] files, boolean recursivelly) throws SVNClientException;

    void setConfigDirectory(VCSFileProxy file) throws SVNClientException;

    void setIgnoredPatterns(VCSFileProxy file, List<String>  l) throws SVNClientException;

    void setPassword(String psswd);

    void setUsername(String user);

    void switchToUrl(VCSFileProxy file, SVNUrl url, SVNRevision rev, boolean rec) throws SVNClientException;

    long update(VCSFileProxy file, SVNRevision rev, boolean recursivelly) throws SVNClientException;

    void upgrade(VCSFileProxy wcRoot) throws SVNClientException;

    public void unlock(VCSFileProxy[] vcsFileProxy, boolean b) throws SVNClientException;

    public void lock(VCSFileProxy[] vcsFileProxy, String string, boolean b) throws SVNClientException;
    
}
