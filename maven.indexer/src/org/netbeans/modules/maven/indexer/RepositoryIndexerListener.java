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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
/*
 * Contributor(s): theanuradha@netbeans.org
 */

package org.netbeans.modules.maven.indexer;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactScanningListener;
import org.apache.maven.index.ScanningResult;
import org.apache.maven.index.context.IndexingContext;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle.Messages;

public class RepositoryIndexerListener implements ArtifactScanningListener, Cancellable {

    private final IndexingContext indexingContext;
    private int count;
    private ProgressHandle handle;
    private final AtomicBoolean canceled = new AtomicBoolean();
    private final RepositoryInfo ri;
    private final Set<File> expectedDirs = new HashSet<File>();
    private final Set<File> encounteredDirs = new HashSet<File>();

    @SuppressWarnings("LeakingThisInConstructor")
    public RepositoryIndexerListener(IndexingContext indexingContext) {
        this.indexingContext = indexingContext;
        ri = RepositoryPreferences.getInstance().getRepositoryInfoById(indexingContext.getId());
        Cancellation.register(this);
    }

    @Messages({
        "# {0} - repo name", "LBL_indexing_repo=Indexing Maven repository: {0}",
        "LBL_findIndexableDirs=Counting indexable directories..."
    })
    @Override public void scanningStarted(IndexingContext ctx) {
        if (handle != null) {
            handle.finish();
        }
        expectedDirs.clear();
        encounteredDirs.clear();
        handle = ProgressHandle.createHandle(Bundle.LBL_indexing_repo(ri != null ? ri.getName() : indexingContext.getId()), this);
        handle.start();
        handle.progress(Bundle.LBL_findIndexableDirs());
        findIndexableDirs(ctx.getRepository());
        handle.switchToDeterminate(expectedDirs.size());
    }
    
    private void findIndexableDirs(File d) {
        // Try to guess what DefaultScanner might find. Hard to know for sure, so guess that nonempty leaf dirs will contain real artifacts.
        if (d == null || d.getName().startsWith(".")) {
            return;
        }
        File[] kids = d.listFiles();
        if (kids == null) {
            return;
        }
        boolean hasFiles = false;
        boolean hasDirs = false;
        for (File f : kids) {
            if (f.isFile() && !f.getName().matches("maven-metadata.*[.]xml")) {
                hasFiles = true;
            }
            if (f.isDirectory()) {
                hasDirs = true;
                findIndexableDirs(f);
            }
        }
        if (hasFiles && !hasDirs) {
            expectedDirs.add(d);
        }
    }

    public @Override boolean cancel() {
        return canceled.compareAndSet(false, true);
    }

    @Override public void artifactDiscovered(ArtifactContext ac) {
        if (canceled.get()) {
            throw new Cancellation();
        }
        count++;
        if (handle != null) {
            String label = ac.getArtifactInfo().getGroupId() + ":" + ac.getArtifactInfo().getArtifactId() + ":" + ac.getArtifactInfo().getVersion();
            File art = ac.getArtifact();
            if (art == null) {
                art = ac.getPom();
            }
            if (art != null) {
                File d = art.getParentFile();
                if (expectedDirs.contains(d)) {
                    encounteredDirs.add(d);
                }
            }
            handle.progress(label, encounteredDirs.size());
        }
    }

    @Override public void artifactError(ArtifactContext ac, Exception e) {
        if (canceled.get()) {
            throw new Cancellation();
        }
    }

    @Override public void scanningFinished(IndexingContext ctx, ScanningResult result) {
//        Set<File> unencountered = new TreeSet<File>(expectedDirs);
//        unencountered.removeAll(encounteredDirs);
//        System.err.println("did not encounter " + unencountered.size() + ":");
//        for (File d : unencountered) {
//            System.err.println("  " + d);
//        }
    }

    void close() {
        if (handle != null) {
            handle.finish();
        }
    }
}
