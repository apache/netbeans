/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
            specs.add(new RefSpec(refSpec));
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
            Map<String, String> remoteTags = Utils.refsToTags(pushResult.getAdvertisedRefs());
            processMessages(pushResult.getMessages());
            Map<String, GitTransportUpdate> remoteRepositoryUpdates = new HashMap<String, GitTransportUpdate>(pushResult.getRemoteUpdates().size());
            for (RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
                GitTransportUpdate upd = getClassFactory().createTransportUpdate(transport.getURI(), update, remoteBranches, remoteTags);
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
