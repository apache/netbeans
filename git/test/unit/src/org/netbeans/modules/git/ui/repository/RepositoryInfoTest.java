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

package org.netbeans.modules.git.ui.repository;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.MockServices;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitRepositoryState;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.modules.git.AbstractGitTestCase;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitVCS;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.masterfs.VersioningAnnotationProvider;
import org.netbeans.modules.versioning.util.Utils;

/**
 *
 * @author ondra
 */
public class RepositoryInfoTest extends AbstractGitTestCase {

    public RepositoryInfoTest (String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {
            VersioningAnnotationProvider.class,
            GitVCS.class});
        System.setProperty("versioning.git.handleExternalEvents", "false");
    }

    public void testSingleton () {
        RepositoryInfo info = RepositoryInfo.getInstance(repositoryLocation);
        RepositoryInfo info2 = RepositoryInfo.getInstance(new File(repositoryLocation.getParentFile(), repositoryLocation.getName()));
        assertTrue(info == info2);
    }

    public void testNullValueForNoRepository () {
        File repo2 = new File(repositoryLocation.getParentFile(), "other");
        repo2.mkdirs();
        assertNull(RepositoryInfo.getInstance(repo2));
    }

    public void testInstanceCollected () throws Exception {
        File repo2 = new File(repositoryLocation.getParentFile(), "other");
        getClient(repo2).init(GitUtils.NULL_PROGRESS_MONITOR);
        assertTrue(repo2.exists());
        Git.getInstance().versionedFilesChanged();
        assertEquals(repo2, Git.getInstance().getRepositoryRoot(repo2));
        RepositoryInfo info = RepositoryInfo.getInstance(repo2);
        assertNotNull(info);

        // delete the repo, all is collected?
        Utils.deleteRecursively(repo2);
        assertFalse(repo2.exists());
        Git.getInstance().versionedFilesChanged();
        assertNull(RepositoryInfo.getInstance(repo2));
        repo2 = null;
        System.gc();
        Field f = RepositoryInfo.class.getDeclaredField("cache");
        f.setAccessible(true);
        Map<File, RepositoryInfo> cache = (Map<File, RepositoryInfo>) f.get(RepositoryInfo.class);
        for (Map.Entry<File, RepositoryInfo> e : cache.entrySet()) {
            if (e.getKey().equals(new File(repositoryLocation.getParentFile(), "other"))) {
                fail("Not collected");
            }
        }
    }

    public void testRefresh () throws Exception {
        File f = new File(repositoryLocation, "file");
        File[] roots = new File[] { f };
        GitClient client = getClient(repositoryLocation);
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        GitRevisionInfo revInfo = client.commit(roots, "bla", null, null, GitUtils.NULL_PROGRESS_MONITOR);
        RepositoryInfo info = RepositoryInfo.getInstance(repositoryLocation);
        info.refresh();
        assertEquals(revInfo.getRevision(), info.getActiveBranch().getId());

        // test refresh
        write(f, "huhu 1");
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        revInfo = client.commit(roots, "bla", null, null, GitUtils.NULL_PROGRESS_MONITOR);
        info.refresh();
        assertEquals(revInfo.getRevision(), info.getActiveBranch().getId());
    }

    public void testChangeHead () throws Exception {
        File f = new File(repositoryLocation, "file");
        File[] roots = new File[] { f };
        GitClient client = getClient(repositoryLocation);
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        GitRevisionInfo revInfo = client.commit(roots, "bla", null, null, GitUtils.NULL_PROGRESS_MONITOR);
        RepositoryInfo info = RepositoryInfo.getInstance(repositoryLocation);
        info.refresh();
        assertEquals(revInfo.getRevision(), info.getActiveBranch().getId());

        // test property support
        GitBranch oldBranch = info.getActiveBranch();
        RepositoryInfoListener list = new RepositoryInfoListener();
        info.addPropertyChangeListener(list);
        write(f, "huhu 2");
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        revInfo = client.commit(roots, "bla", null, null, GitUtils.NULL_PROGRESS_MONITOR);
        info.refresh();
        assertEquals(revInfo.getRevision(), info.getActiveBranch().getId());
        list.assertPropertyEvent(RepositoryInfo.PROPERTY_HEAD, oldBranch, info.getActiveBranch());
    }

    public void testChangeBranch () throws Exception {
        File f = new File(repositoryLocation, "file");
        File[] roots = new File[] { f };
        GitClient client = getClient(repositoryLocation);
        RepositoryInfo info = RepositoryInfo.getInstance(repositoryLocation);
        info.refresh();
        assertEquals(GitBranch.NO_BRANCH, info.getActiveBranch().getName());
        assertEquals(AbstractGitTestCase.NULL_OBJECT_ID, info.getActiveBranch().getId());
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);

        // test property support
        GitBranch oldBranch = info.getActiveBranch();
        RepositoryInfoListener list = new RepositoryInfoListener();
        info.addPropertyChangeListener(list);
        write(f, "huhu");
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        client.commit(roots, "bla", null, null, GitUtils.NULL_PROGRESS_MONITOR);
        info.refresh();
        list.assertPropertyEvent(RepositoryInfo.PROPERTY_ACTIVE_BRANCH, oldBranch, info.getActiveBranch());

    }

    public void testChangeState () throws Exception {
        File f = new File(repositoryLocation, "file");
        File[] roots = new File[] { f };
        GitClient client = getClient(repositoryLocation);
        RepositoryInfo info = RepositoryInfo.getInstance(repositoryLocation);
        info.refresh();
        assertEquals(GitRepositoryState.SAFE, info.getRepositoryState());

        RepositoryInfoListener list = new RepositoryInfoListener();
        info.addPropertyChangeListener(list);
        File mergeFlag = new File(new File(repositoryLocation, ".git"), "MERGE_HEAD");
        mergeFlag.createNewFile();
        info.refresh();
        list.assertPropertyEvent(RepositoryInfo.PROPERTY_STATE, GitRepositoryState.SAFE, GitRepositoryState.MERGING_RESOLVED);
        info.removePropertyChangeListener(list);

        info.addPropertyChangeListener(list = new RepositoryInfoListener());
        mergeFlag.delete();
        info.refresh();
        list.assertPropertyEvent(RepositoryInfo.PROPERTY_STATE, GitRepositoryState.MERGING_RESOLVED, GitRepositoryState.SAFE);
    }

    private static class RepositoryInfoListener implements PropertyChangeListener {
        List<PropertyChangeEvent> events = new LinkedList<PropertyChangeEvent>();
        @Override
        public void propertyChange (PropertyChangeEvent evt) {
            events.add(evt);
        }

        private void assertPropertyEvent (String prop, Object oldValue, Object newValue) {
            boolean ok = false;
            for (PropertyChangeEvent evt : events) {
                if (prop.equals(evt.getPropertyName())) {
                    assertEquals(oldValue, evt.getOldValue());
                    assertEquals(newValue, evt.getNewValue());
                    ok = true;
                }
            }
            assertTrue(ok);
        }
    }
}
