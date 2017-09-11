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

package org.netbeans.modules.git.ui.merge;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitMergeResult;
import org.netbeans.libs.git.GitMergeResult.MergeStatus;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.modules.git.AbstractGitTestCase;
import org.netbeans.modules.git.utils.GitUtils;

/**
 *
 * @author ondra
 */
public class MergeTest extends AbstractGitTestCase {

    public MergeTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Add notification listener in Merge action when git lib starts sending notifications.
     */
    public void testNotifiedFiles () throws Exception {
        File f = new File(repositoryLocation, "file");
        f.createNewFile();
        GitClient client = getClient(repositoryLocation);
        File[] roots = { f };
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        client.commit(roots, "initial", null, null, GitUtils.NULL_PROGRESS_MONITOR);
        String branchName = "nova";
        assertEquals(branchName, client.createBranch(branchName, "master", GitUtils.NULL_PROGRESS_MONITOR).getName());
        client.checkoutRevision(branchName, true, GitUtils.NULL_PROGRESS_MONITOR);
        write(f, "blablabla");
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        client.commit(roots, branchName, null, null, GitUtils.NULL_PROGRESS_MONITOR);
        client.checkoutRevision("master", true, GitUtils.NULL_PROGRESS_MONITOR);
        assertEquals("", read(f));
        
        final Set<File> notifiedFiles = new HashSet<File>();
        client.addNotificationListener(new FileListener() {
            @Override
            public void notifyFile (File file, String relativePathToRoot) {
                notifiedFiles.add(file);
            }
        });
        GitMergeResult result = client.merge(branchName, GitUtils.NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.FAST_FORWARD, result.getMergeStatus());
        assertEquals("blablabla", read(f));
        
        assertEquals(0, notifiedFiles.size());
    }
}
