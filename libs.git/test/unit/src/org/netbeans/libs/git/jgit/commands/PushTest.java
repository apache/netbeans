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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitPushResult;
import org.netbeans.libs.git.GitRefUpdateResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.libs.git.GitTransportUpdate.Type;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.jgit.DelegatingProgressMonitor;

/**
 *
 * @author ondra
 */
public class PushTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;
    private File otherWT;
    private File f;
    private GitRevisionInfo masterInfo;
    private GitBranch branch;

    public PushTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }
    
    public void testPushNewBranch () throws Exception {
        String remoteUri = getRemoteRepository().getWorkTree().toURI().toString();
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR).size());
        File f = new File(workDir, "f");
        add(f);
        String id = getClient(workDir).commit(new File[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);

        // adding another branch
        write(f, "huhu");
        add(f);
        String newid = getClient(workDir).commit(new File[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/anotherBranch" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(2, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(newid, remoteBranches.get("anotherBranch").getId());
        assertUpdate(updates.get("anotherBranch"), "master", "anotherBranch", newid, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
    }
    
    public void testPushDeleteBranch () throws Exception {
        String remoteUri = getRemoteRepository().getWorkTree().toURI().toString();
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR).size());
        File f = new File(workDir, "f");
        add(f);
        String id = getClient(workDir).commit(new File[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master", "refs/heads/master:refs/heads/newbranch" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(2, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(2, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
        assertUpdate(updates.get("newbranch"), "master", "newbranch", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);

        // deleting branch
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { ":refs/heads/newbranch" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertUpdate(updates.get("newbranch"), null, "newbranch", null, id, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
    }
    
    public void testPushChange () throws Exception {
        String remoteUri = getRemoteRepository().getWorkTree().toURI().toString();
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR).size());
        File f = new File(workDir, "f");
        add(f);
        String id = getClient(workDir).commit(new File[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);

        // modification
        write(f, "huhu");
        add(f);
        String newid = getClient(workDir).commit(new File[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(newid, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", newid, id, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
    }
    
    public void testPushUpdateInRemotes () throws Exception {
        String remoteUri = getRemoteRepository().getWorkTree().toURI().toString();
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR).size());
        File f = new File(workDir, "f");
        add(f);
        String id = getClient(workDir).commit(new File[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
        
        getClient(workDir).pull(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/remotes/origin/master" }), "master", NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = getClient(workDir).getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertEquals(id, branches.get("origin/master").getId());

        // modification
        write(f, "huhu");
        add(f);
        String newid = getClient(workDir).commit(new File[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        GitPushResult result = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR);
        updates = result.getRemoteRepositoryUpdates();
        Map<String, GitTransportUpdate> localUpdates = result.getLocalRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        branches = getClient(workDir).getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        // not yet updated, tracking branches has not been set
        assertEquals(0, localUpdates.size());
        assertEquals(id, branches.get("origin/master").getId());
        
        // another modification
        write(f, "huhu2");
        add(f);
        newid = getClient(workDir).commit(new File[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        result = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Arrays.asList(new String[] { "refs/heads/master:refs/remotes/origin/master" }), NULL_PROGRESS_MONITOR);
        updates = result.getRemoteRepositoryUpdates();
        localUpdates = result.getLocalRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        branches = getClient(workDir).getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertEquals(1, localUpdates.size());
        assertUpdate(localUpdates.get("master"), "origin/master", "master", newid, id, new URIish(remoteUri).toString(), Type.BRANCH, EnumSet.of(GitRefUpdateResult.FAST_FORWARD, GitRefUpdateResult.FORCED));
        assertEquals(newid, branches.get("origin/master").getId());
        
        //let's set tracking branch
        StoredConfig cfg = repository.getConfig();
        cfg.setString(ConfigConstants.CONFIG_REMOTE_SECTION, "origin", ConfigConstants.CONFIG_KEY_URL, new URIish(remoteUri).toString());
        cfg.setString(ConfigConstants.CONFIG_REMOTE_SECTION, "origin", "fetch", "+refs/heads/master:refs/remotes/origin/master");
        cfg.setString(ConfigConstants.CONFIG_BRANCH_SECTION, "master", ConfigConstants.CONFIG_KEY_REMOTE, "origin");
        cfg.setString(ConfigConstants.CONFIG_BRANCH_SECTION, "master", ConfigConstants.CONFIG_KEY_MERGE, "refs/heads/master");
        cfg.save();
        
        // what about now???
        write(f, "huhu3");
        add(f);
        id = newid;
        newid = getClient(workDir).commit(new File[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        result = getClient(workDir).push("origin", Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR);
        updates = result.getRemoteRepositoryUpdates();
        localUpdates = result.getLocalRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertUpdate(updates.get("master"), "master", "master", newid, id, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
        branches = getClient(workDir).getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertEquals(1, localUpdates.size());
        assertUpdate(localUpdates.get("master"), "origin/master", "master", newid, id, new URIish(remoteUri).toString(), Type.BRANCH, EnumSet.of(GitRefUpdateResult.FAST_FORWARD, GitRefUpdateResult.FORCED));
        assertEquals(newid, branches.get("origin/master").getId());
        
        // and what about adding a new branch, does it show among remotes?
        result = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/newbranch" }), Arrays.asList(new String[] { "refs/heads/newbranch:refs/remotes/origin/newbranch" }), NULL_PROGRESS_MONITOR);
        updates = result.getRemoteRepositoryUpdates();
        localUpdates = result.getLocalRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(2, remoteBranches.size());
        branches = getClient(workDir).getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(3, branches.size());
        assertEquals(1, localUpdates.size());
        assertUpdate(localUpdates.get("newbranch"), "origin/newbranch", "newbranch", newid, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.NEW);
        assertEquals(newid, branches.get("origin/newbranch").getId());
    }
    
    public void testPushRejectNonFastForward () throws Exception {
        String remoteUri = getRemoteRepository().getWorkTree().toURI().toString();
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR).size());
        File f = new File(workDir, "f");
        add(f);
        String id = getClient(workDir).commit(new File[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);

        // modification
        write(f, "huhu");
        add(f);
        String newid = getClient(workDir).commit(new File[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(newid, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", newid, id, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
        
        getClient(workDir).createBranch("localbranch", id, NULL_PROGRESS_MONITOR);
        getClient(workDir).checkoutRevision("localbranch", true, NULL_PROGRESS_MONITOR);
        write(f, "huhu2");
        add(f);
        id = getClient(workDir).commit(new File[] { f }, "some change before merge", null, null, NULL_PROGRESS_MONITOR).getRevision();
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "+refs/heads/localbranch:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(newid, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "localbranch", "master", id, newid, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.REJECTED_NONFASTFORWARD);
        
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/localbranch:refs/heads/master" }), Arrays.asList(new String[] { "+refs/heads/master:refs/remotes/origin/master" }), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(newid, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "localbranch", "master", id, newid, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.REJECTED_NONFASTFORWARD);
        
        // if starts failing, the WA at GitTransportUpdate.(URIish uri, TrackingRefUpdate update) should be removed
        // this.result = GitRefUpdateResult.valueOf((update.getResult() == null ? RefUpdate.Result.NOT_ATTEMPTED : update.getResult()).name());
        Transport transport = Transport.open(getRepository(getClient(workDir)), new URIish(remoteUri));
        transport.setDryRun(false);
        transport.setPushThin(true);
        PushResult pushResult = transport.push(new DelegatingProgressMonitor(NULL_PROGRESS_MONITOR),
                Transport.findRemoteRefUpdatesFor(getRepository(getClient(workDir)),
                Collections.singletonList(new RefSpec("refs/heads/localbranch:refs/heads/master")),
                Collections.singletonList(new RefSpec("refs/heads/master:refs/remotes/origin/master"))));
        assertEquals(1, pushResult.getTrackingRefUpdates().size());
        for (TrackingRefUpdate update : pushResult.getTrackingRefUpdates()) {
            // null but not NOT_ATTEMPTED, probably a bug
            // remove the WA if it starts failing here
            assertNull(update.getResult());
        }
    }

    public void testPushTag () throws Exception {
        String remoteUri = getRemoteRepository().getWorkTree().toURI().toString();
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR).size());
        File f = new File(workDir, "f");
        add(f);
        String id = getClient(workDir).commit(new File[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        GitTag tag = getClient(workDir).createTag("my-tag", id, "tag message", false, false, NULL_PROGRESS_MONITOR);
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master", "refs/tags/my-tag:refs/tags/my-tag" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        Map<String, String> remoteTags = getClient(workDir).listRemoteTags(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteTags.size());
        assertEquals(tag.getTagId(), remoteTags.get("my-tag"));
        assertEquals(2, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
        assertUpdate(updates.get("my-tag"), "my-tag", "my-tag", tag.getTagId(), null, new URIish(remoteUri).toString(), Type.TAG, GitRefUpdateResult.OK);
        
        // modification, updating tag fails when not force update
        write(f, "huhu");
        add(f);
        String newid = getClient(workDir).commit(new File[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        GitTag newTag = getClient(workDir).createTag("my-tag", newid, "tag message", false, true, NULL_PROGRESS_MONITOR);
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master", "refs/tags/my-tag:refs/tags/my-tag" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteTags = getClient(workDir).listRemoteTags(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(tag.getTagId(), remoteTags.get("my-tag"));
        assertEquals(2, updates.size());
        assertUpdate(updates.get("master"), "master", "master", newid, id, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
        assertUpdate(updates.get("my-tag"), "my-tag", "my-tag", newTag.getTagId(), null, new URIish(remoteUri).toString(), Type.TAG, GitRefUpdateResult.REJECTED_NONFASTFORWARD);
        
        // modification, updating tag now works
        write(f, "huhu");
        add(f);
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "+refs/tags/my-tag:refs/tags/my-tag" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteTags = getClient(workDir).listRemoteTags(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(newTag.getTagId(), remoteTags.get("my-tag"));
        assertEquals(1, updates.size());
        assertUpdate(updates.get("my-tag"), "my-tag", "my-tag", newTag.getTagId(), null, new URIish(remoteUri).toString(), Type.TAG, GitRefUpdateResult.OK);
}

    private void assertUpdate(GitTransportUpdate update, String localName, String remoteName, String newObjectId, String oldObjectId, String remoteUri, Type type, GitRefUpdateResult result) {
        assertUpdate(update, localName, remoteName, newObjectId, oldObjectId, remoteUri, type, EnumSet.of(result));
    }

    private void assertUpdate(GitTransportUpdate update, String localName, String remoteName, String newObjectId, String oldObjectId, String remoteUri, Type type,
            EnumSet<GitRefUpdateResult> allowedResults) {
        assertEquals(localName, update.getLocalName());
        assertEquals(remoteName, update.getRemoteName());
        assertEquals(newObjectId, update.getNewObjectId());
        assertEquals(oldObjectId, update.getOldObjectId());
        assertEquals(remoteUri, update.getRemoteUri());
        assertEquals(type, update.getType());
        assertTrue("Result: " + update.getResult() + " not allowed: " + allowedResults, allowedResults.contains(update.getResult()));
    }
}
