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
    private final Set<File> expectedDirs = new HashSet<>();
    private final Set<File> encounteredDirs = new HashSet<>();

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
