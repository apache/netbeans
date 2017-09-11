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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.libs.git.jgit.commands;

import java.io.IOException;
import org.netbeans.libs.git.GitTransportUpdate;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.libs.git.jgit.DelegatingProgressMonitor;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitPushResult;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class PushCommand extends TransportCommand {

    private final ProgressMonitor monitor;
    private final List<String> pushRefSpecs;
    private final String remote;
    private GitPushResult result;
    private final List<String> fetchRefSpecs;

    public PushCommand (Repository repository, GitClassFactory gitFactory, String remote, List<String> pushRefSpecifications, List<String> fetchRefSpecifications, ProgressMonitor monitor) {
        super(repository, gitFactory, remote, monitor);
        this.monitor = monitor;
        this.remote = remote;
        this.pushRefSpecs = pushRefSpecifications;
        this.fetchRefSpecs = fetchRefSpecifications;
    }

    @Override
    protected void runTransportCommand () throws GitException.AuthorizationException, GitException {
        List<RefSpec> specs = new ArrayList<RefSpec>(pushRefSpecs.size());
        for (String refSpec : pushRefSpecs) {
            // this may be extra strict. We do not allow force updates for branches,
            // but maybe we should leave that decision on the caller
            RefSpec sp = new RefSpec(refSpec);
            String source = sp.getSource();
            String dest = sp.getDestination();
            if (source != null && Transport.REFSPEC_TAGS.matchSource(source)
                    && dest != null && Transport.REFSPEC_TAGS.matchDestination(sp.getDestination())) {
                specs.add(sp);
            } else {
                specs.add(sp.setForceUpdate(false));
            }
        }
        // this will ensure that refs/remotes/abc/branch will be updated, too
        List<RefSpec> fetchSpecs = new ArrayList<RefSpec>(fetchRefSpecs == null ? 0 : fetchRefSpecs.size());
        for (String refSpec : fetchRefSpecs) {
            RefSpec sp = new RefSpec(refSpec);
            fetchSpecs.add(sp);
        }
        Transport transport = null;
        try {
            transport = openTransport(true);
            transport.setDryRun(false);
            transport.setPushThin(true);
            transport.setRemoveDeletedRefs(true);
            transport.setTagOpt(TagOpt.AUTO_FOLLOW);
            PushResult pushResult = transport.push(new DelegatingProgressMonitor(monitor), fetchSpecs.isEmpty() ? transport.findRemoteRefUpdatesFor(specs) : Transport.findRemoteRefUpdatesFor(getRepository(), specs, fetchSpecs));
            Map<String, GitBranch> remoteBranches = Utils.refsToBranches(pushResult.getAdvertisedRefs(), Constants.R_HEADS, getClassFactory());
            processMessages(pushResult.getMessages());
            Map<String, GitTransportUpdate> remoteRepositoryUpdates = new HashMap<String, GitTransportUpdate>(pushResult.getRemoteUpdates().size());
            for (RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
                GitTransportUpdate upd = getClassFactory().createTransportUpdate(transport.getURI(), update, remoteBranches);
                remoteRepositoryUpdates.put(upd.getRemoteName(), upd);
            }
            Map<String, GitTransportUpdate> localRepositoryUpdates = new HashMap<String, GitTransportUpdate>(pushResult.getTrackingRefUpdates().size());
            for (TrackingRefUpdate update : pushResult.getTrackingRefUpdates()) {
                GitTransportUpdate upd = getClassFactory().createTransportUpdate(transport.getURI(), update);
                localRepositoryUpdates.put(upd.getRemoteName(), upd);
            }
            result = getClassFactory().createPushResult(remoteRepositoryUpdates, localRepositoryUpdates);
        } catch (NotSupportedException e) {
            throw new GitException(e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new GitException(e.getMessage(), e);
        } catch (TransportException e) {
            URIish uriish = null;
            try {
                uriish = getUriWithUsername(true);
            } catch (URISyntaxException ex) {
                throw new GitException(e.getMessage(), e);
            }
            handleException(e, uriish);
        } catch (IOException e) {
            throw new GitException(e.getMessage(), e);
        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git push ").append(remote); //NOI18N
        for (String refSpec : pushRefSpecs) {
            sb.append(' ').append(refSpec);
        }
        return sb.toString();
    }
    
    public GitPushResult getResult () {
        return result;
    }
}
