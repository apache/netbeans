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

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitPushResult;
import org.netbeans.modules.git.remote.cli.GitRefUpdateResult;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitTag;
import org.netbeans.modules.git.remote.cli.GitTransportUpdate;
import org.netbeans.modules.git.remote.cli.GitTransportUpdate.Type;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitConfig;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.jgit.URIish;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class PushTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;
    private VCSFileProxy otherWT;
    private VCSFileProxy f;
    private GitRevisionInfo masterInfo;
    private GitBranch branch;

    public PushTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testPushUpdateInRemotes").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }
    
    public void testPushNewBranch () throws Exception {
        JGitConfig config = repository.getConfig();
        config.load();
        String remoteUri = config.getString(JGitConfig.CONFIG_KEY_REMOTE, "origin", JGitConfig.CONFIG_KEY_URL);
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR).size());
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxySupport.createNew(f);
        add(f);
        String id = getClient(workDir).commit(new VCSFileProxy[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);

        // adding another branch
        write(f, "huhu");
        add(f);
        String newid = getClient(workDir).commit(new VCSFileProxy[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/anotherBranch" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(2, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(newid, remoteBranches.get("anotherBranch").getId());
        assertUpdate(updates.get("anotherBranch"), "master", "anotherBranch", newid, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
    }
    
    public void testPushDeleteBranch () throws Exception {
        JGitConfig config = repository.getConfig();
        config.load();
        String remoteUri = config.getString(JGitConfig.CONFIG_KEY_REMOTE, "origin", JGitConfig.CONFIG_KEY_URL);
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR).size());
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxySupport.createNew(f);
        add(f);
        String id = getClient(workDir).commit(new VCSFileProxy[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
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
        JGitConfig config = repository.getConfig();
        config.load();
        String remoteUri = config.getString(JGitConfig.CONFIG_KEY_REMOTE, "origin", JGitConfig.CONFIG_KEY_URL);
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR).size());
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxySupport.createNew(f);
        add(f);
        String id = getClient(workDir).commit(new VCSFileProxy[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);

        // modification
        write(f, "huhu");
        add(f);
        String newid = getClient(workDir).commit(new VCSFileProxy[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(newid, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", newid, id, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
    }
    
    public void testPushUpdateInRemotes () throws Exception {
        JGitConfig config = repository.getConfig();
        config.load();
        String remoteUri = config.getString(JGitConfig.CONFIG_KEY_REMOTE, "origin", JGitConfig.CONFIG_KEY_URL);
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR).size());
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxySupport.createNew(f);
        add(f);
        String id = getClient(workDir).commit(new VCSFileProxy[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
        
        getClient(workDir).pull(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/remotes/origin/master" }), "master", NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = getClient(workDir).getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
if (false)assertEquals(id, branches.get("origin/master").getId());
else    assertEqualsID(id, branches.get("origin/master").getId());

        // modification
        write(f, "huhu");
        add(f);
        String newid = getClient(workDir).commit(new VCSFileProxy[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        GitPushResult result = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR);
        updates = result.getRemoteRepositoryUpdates();
        Map<String, GitTransportUpdate> localUpdates = result.getLocalRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        branches = getClient(workDir).getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        // not yet updated, tracking branches has not been set
        assertEquals(0, localUpdates.size());
if(false)assertEquals(id, branches.get("origin/master").getId());
else    assertEqualsID(id, branches.get("origin/master").getId());
        
        // another modification
        write(f, "huhu2");
        add(f);
        newid = getClient(workDir).commit(new VCSFileProxy[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        result = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Arrays.asList(new String[] { "refs/heads/master:refs/remotes/origin/master" }), NULL_PROGRESS_MONITOR);
        updates = result.getRemoteRepositoryUpdates();
        localUpdates = result.getLocalRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        branches = getClient(workDir).getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertEquals(1, localUpdates.size());
        assertUpdate(localUpdates.get("master"), "origin/master", "master", newid, id, new URIish(remoteUri).toString(), Type.BRANCH, EnumSet.of(GitRefUpdateResult.OK, GitRefUpdateResult.FAST_FORWARD, GitRefUpdateResult.FORCED));
if(false)assertEquals(newid, branches.get("master").getId());
else    assertEqualsID(newid, branches.get("master").getId());
        
        //let's set tracking branch
        JGitConfig cfg = repository.getConfig();
        cfg.setString(JGitConfig.CONFIG_REMOTE_SECTION, "origin", JGitConfig.CONFIG_KEY_URL, new URIish(remoteUri).toString());
        cfg.setString(JGitConfig.CONFIG_REMOTE_SECTION, "origin", JGitConfig.CONFIG_KEY_FETCH, "+refs/heads/master:refs/remotes/origin/master");
        cfg.setString(JGitConfig.CONFIG_BRANCH_SECTION, "master", JGitConfig.CONFIG_KEY_REMOTE, "origin");
        cfg.setString(JGitConfig.CONFIG_BRANCH_SECTION, "master", JGitConfig.CONFIG_KEY_MERGE, "refs/heads/master");
        cfg.save();
        
        // what about now???
        write(f, "huhu3");
        add(f);
        id = newid;
        newid = getClient(workDir).commit(new VCSFileProxy[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
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
        JGitConfig config = repository.getConfig();
        config.load();
        String remoteUri = config.getString(JGitConfig.CONFIG_KEY_REMOTE, "origin", JGitConfig.CONFIG_KEY_URL);
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR).size());
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxySupport.createNew(f);
        add(f);
        String id = getClient(workDir).commit(new VCSFileProxy[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitTransportUpdate> updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "master", "master", id, null, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);

        // modification
        write(f, "huhu");
        add(f);
        String newid = getClient(workDir).commit(new VCSFileProxy[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
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
        id = getClient(workDir).commit(new VCSFileProxy[] { f }, "some change before merge", null, null, NULL_PROGRESS_MONITOR).getRevision();
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "+refs/heads/localbranch:refs/heads/master" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "localbranch", "master", id, newid, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
        
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/localbranch:refs/heads/master" }), Arrays.asList(new String[] { "+refs/heads/master:refs/remotes/origin/master" }), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteBranches = getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(1, remoteBranches.size());
        assertEquals(id, remoteBranches.get("master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("master"), "localbranch", "master", id, newid, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.UP_TO_DATE);
        
        // if starts failing, the WA at GitTransportUpdate.(URIish uri, TrackingRefUpdate update) should be removed
        // this.result = GitRefUpdateResult.valueOf((update.getResult() == null ? RefUpdate.Result.NOT_ATTEMPTED : update.getResult()).name());
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/localbranch:refs/heads/master" }), Arrays.asList(new String[] { "+refs/heads/master:refs/remotes/origin/master" }), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        assertEquals(1, updates.size());
    }

    public void testPushTag () throws Exception {
        JGitConfig config = repository.getConfig();
        config.load();
        String remoteUri = config.getString(JGitConfig.CONFIG_KEY_REMOTE, "origin", JGitConfig.CONFIG_KEY_URL);
        assertEquals(0, getClient(workDir).listRemoteBranches(remoteUri, NULL_PROGRESS_MONITOR).size());
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxySupport.createNew(f);
        add(f);
        String id = getClient(workDir).commit(new VCSFileProxy[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
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
        String newid = getClient(workDir).commit(new VCSFileProxy[] { f }, "bbb", null, null, NULL_PROGRESS_MONITOR).getRevision();
        GitTag newTag = getClient(workDir).createTag("my-tag1", newid, "tag message", false, true, NULL_PROGRESS_MONITOR);
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "refs/heads/master:refs/heads/master", "refs/tags/my-tag1:refs/tags/my-tag1" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteTags = getClient(workDir).listRemoteTags(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(newTag.getTagId(), remoteTags.get("my-tag1"));
        assertEquals(2, updates.size());
        assertUpdate(updates.get("master"), "master", "master", newid, id, new URIish(remoteUri).toString(), Type.BRANCH, GitRefUpdateResult.OK);
        assertUpdate(updates.get("my-tag1"), "my-tag1", "my-tag1", newTag.getTagId(), null, new URIish(remoteUri).toString(), Type.TAG, GitRefUpdateResult.OK);
        
        // modification, updating tag now works
        write(f, "huhu1");
        add(f);
        updates = getClient(workDir).push(remoteUri, Arrays.asList(new String[] { "+refs/tags/my-tag:refs/tags/my-tag" }), Collections.<String>emptyList(), NULL_PROGRESS_MONITOR).getRemoteRepositoryUpdates();
        remoteTags = getClient(workDir).listRemoteTags(remoteUri, NULL_PROGRESS_MONITOR);
        assertEquals(newTag.getTagId(), remoteTags.get("my-tag1"));
        assertEquals(1, updates.size());
        assertUpdate(updates.get("my-tag"), "my-tag", "my-tag", newTag.getTagId(), null, new URIish(remoteUri).toString(), Type.TAG, GitRefUpdateResult.UP_TO_DATE);
}

    private void assertUpdate(GitTransportUpdate update, String localName, String remoteName, String newObjectId, String oldObjectId, String remoteUri, Type type, GitRefUpdateResult result) {
        assertUpdate(update, localName, remoteName, newObjectId, oldObjectId, remoteUri, type, EnumSet.of(result));
    }

    private void assertUpdate(GitTransportUpdate update, String localName, String remoteName, String newObjectId, String oldObjectId, String remoteUri, Type type,
            EnumSet<GitRefUpdateResult> allowedResults) {
        assertEquals(localName, update.getLocalName());
        assertEquals(remoteName, update.getRemoteName());
        //assertEquals(newObjectId, update.getNewObjectId());
        //assertEquals(oldObjectId, update.getOldObjectId());
        assertEquals(remoteUri, update.getRemoteUri());
        assertEquals(type, update.getType());
        assertTrue("Result: " + update.getResult() + " not allowed: " + allowedResults, allowedResults.contains(update.getResult()));
    }
}
