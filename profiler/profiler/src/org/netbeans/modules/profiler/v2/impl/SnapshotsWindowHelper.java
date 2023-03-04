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

package org.netbeans.modules.profiler.v2.impl;

import java.io.File;
import org.netbeans.modules.profiler.LoadedSnapshot;
import org.netbeans.modules.profiler.ResultsManager;
import org.netbeans.modules.profiler.SnapshotResultsWindow;
import org.netbeans.modules.profiler.SnapshotsListener;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.v2.SnapshotsWindow;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider(service=SnapshotsListener.class)
public final class SnapshotsWindowHelper implements SnapshotsListener {
    
    static final WeakProcessor PROCESSOR = new WeakProcessor("Snapshots Window Processor"); // NOI18N
    
    public void snapshotTaken(LoadedSnapshot snapshot) {
        if (ProfilerIDESettings.getInstance().getAutoOpenSnapshot()) {
//            int sortingColumn = LiveResultsWindow.hasDefault() ? LiveResultsWindow.getDefault().getSortingColumn()
//                                                               : CommonConstants.SORTING_COLUMN_DEFAULT;
//            boolean sortingOrder = LiveResultsWindow.hasDefault() ? LiveResultsWindow.getDefault().getSortingOrder() : false;
//            ResultsManager.getDefault().openSnapshot(ls, sortingColumn, sortingOrder);

            ResultsManager.getDefault().openSnapshot(snapshot);
        }

        if (ProfilerIDESettings.getInstance().getAutoSaveSnapshot()) {
            ResultsManager.getDefault().saveSnapshot(snapshot);
            if (!ProfilerIDESettings.getInstance().getAutoOpenSnapshot())
                ResultsManager.getDefault().closeSnapshot(snapshot);
        }
    }

    public void snapshotLoaded(LoadedSnapshot snapshot) {}

    public void snapshotSaved(final LoadedSnapshot snapshot) {
        refreshSnapshots(snapshot);
        PROCESSOR.post(new Runnable() {
            public void run() { SnapshotsWindow.instance().snapshotSaved(snapshot); }
        });
    }

    public void snapshotRemoved(LoadedSnapshot snapshot) {
        SnapshotResultsWindow.closeWindow(snapshot);
        refreshSnapshots(snapshot);
    }

    private void refreshSnapshots(final LoadedSnapshot snapshot) {
        PROCESSOR.post(new Runnable() {
            public void run() {
                File f = snapshot.getFile();
                File p = f == null ? null : f.getParentFile();
                FileObject fo = p == null ? null : FileUtil.toFileObject(p);
                if (fo != null) SnapshotsWindow.instance().refreshFolder(fo, true);
            }
        });
    }
    
}
