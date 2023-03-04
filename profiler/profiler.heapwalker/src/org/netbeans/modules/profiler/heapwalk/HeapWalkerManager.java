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

package org.netbeans.modules.profiler.heapwalk;

import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.global.Platform;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.netbeans.modules.profiler.v2.SnapshotsWindow;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * Manages HeapWalker instances & TopComponents
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "HeapWalkerManager_CannotOpenHeapWalkerMsg=Failed to open the heap dump.",
    "HeapWalkerManager_CannotDeleteHeapDumpMsg=<html><b>Deleting heap dump failed</b><br><br>Please try to delete the file once more. If it fails<br>again, restart the IDE and repeat the action.</html>"
})
public class HeapWalkerManager {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Set dumpsBeingDeleted = new HashSet<>();
    private List<HeapWalker> heapWalkers = new ArrayList<>();

    private final RequestProcessor heapwalkerRp = new RequestProcessor(HeapWalkerManager.class);

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    private HeapWalkerManager() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    private static class Singleton {
        private static final HeapWalkerManager INSTANCE = new HeapWalkerManager();
    }
    
    public static HeapWalkerManager getDefault() {
        return Singleton.INSTANCE;
    }

    public boolean isHeapWalkerOpened(File file) {
        return getHeapWalker(file, 0) != null;
    }

    public void closeAllHeapWalkers() {
        HeapWalker[] heapWalkerArr;
        synchronized (this) {
            heapWalkerArr = heapWalkers.toArray(new HeapWalker[0]);
        }
        for (HeapWalker hw : heapWalkerArr) {
            closeHeapWalker(hw);
        }
    }

    public void closeHeapWalker(final HeapWalker hw) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    final TopComponent tc = getTopComponent(hw);

                    if (tc == null) {
                        ErrorManager.getDefault().log("Cannot resolve TopComponent for HeapWalker [" + hw.getHeapDumpFile() + "]"); // NOI18N

                        return;
                    }

                    tc.close();
                    FileObject folder = FileUtil.toFileObject(hw.getHeapDumpFile().getParentFile());
                    SnapshotsWindow.instance().refreshFolder(folder, false);
//                    if (ProfilerControlPanel2.hasDefault())
//                        ProfilerControlPanel2.getDefault().refreshSnapshotsList(); // Refresh to display closed HW using plain font
                }
            });
    }

    public void deleteHeapDump(final File file) {
        HeapWalker hw = getHeapWalker(file, 0);

        if (hw != null) {
            dumpsBeingDeleted.add(file);
            closeHeapWalker(hw);
        } else {
            deleteHeapDumpImpl(file, 15);
        }
    }

    // should only be called from HeapWalkerUI.componentClosed
    public synchronized void heapWalkerClosed(HeapWalker hw) {
        final TopComponent tc = getTopComponent(hw);

        if (tc == null) {
            return;
        }

        final File file = hw.getHeapDumpFile();
        heapWalkers.remove(hw);

        if (dumpsBeingDeleted.remove(file)) {
            BrowserUtils.performTask(new Runnable() {
                    public void run() {
                        deleteHeapDumpImpl(file, 15);
                    }
                });

        }
    }

    public void openHeapWalker(final File heapDump) {
        openHeapWalker(heapDump, 0);
    }

    public void openHeapWalker(final File heapDump, int segment) {
        String heapDumpPath;
        
        try {
            heapDumpPath = heapDump.getCanonicalPath();
        } catch (IOException ex) {
            ProfilerDialogs.displayError(Bundle.HeapWalkerManager_CannotOpenHeapWalkerMsg(), null, ex.getLocalizedMessage());
            return;
        }
        synchronized (heapDumpPath.intern()) {
            HeapWalker hw = getHeapWalker(heapDump, segment);

            if (hw == null) {
                try {
                    hw = new HeapWalker(heapDump, segment);
                } catch (IOException e) {
                    ProfilerDialogs.displayError(Bundle.HeapWalkerManager_CannotOpenHeapWalkerMsg(), null, e.getLocalizedMessage());
                } catch (Exception e) {
                    Logger.getLogger(HeapWalkerManager.class.getName()).log(Level.SEVERE, null, e);
                }
            }

            if (hw != null) {
                openHeapWalker(hw);
            } else {
                ProfilerLogger.severe("Cannot create HeapWalker [" + heapDump + "]"); // NOI18N
            }
        }
    }

    public synchronized void openHeapWalker(final HeapWalker hw) {
        if (!heapWalkers.contains(hw)) {
            heapWalkers.add(hw);
        }
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    final TopComponent tc = getTopComponent(hw);

                    if (tc == null) {
                        ProfilerLogger.severe("Cannot resolve TopComponent for HeapWalker [" + hw.getHeapDumpFile() + "]"); // NOI18N

                        return;
                    }

                    tc.open();
                    //        tc.requestActive(); // For some reason steals focus from Dump Heap button in ProfilerControlPanel2 and causes http://www.netbeans.org/issues/show_bug.cgi?id=92425
                    tc.requestVisible(); // Workaround for the above problem
                    
                    FileObject folder = FileUtil.toFileObject(hw.getHeapDumpFile().getParentFile());
                    SnapshotsWindow.instance().refreshFolder(folder, false);
//                    if (ProfilerControlPanel2.hasDefault())
//                        ProfilerControlPanel2.getDefault().refreshSnapshotsList(); // Refresh to display opened HW using bold font
                }
            });
    }

    public void openHeapWalkers(File[] heapDumps) {
        for (File heapDump : heapDumps) {
            openHeapWalker(heapDump);
        }
    }

    private synchronized HeapWalker getHeapWalker(File heapDump, int segment) {
        for (HeapWalker hw : heapWalkers) {
            if (hw.getHeapDumpFile().equals(heapDump) && hw.getHeapDumpSegment() == segment) {
                return hw;
            }
        }
        return null;
    }

    private TopComponent getTopComponent(HeapWalker hw) {
        assert SwingUtilities.isEventDispatchThread();
        return hw.getTopComponent();
    }

    private void deleteHeapDumpImpl(final File file, final int retries) {
        heapwalkerRp.post(new Runnable() {
                public void run() {
                    if (!file.delete()) {
                        if ((retries > 0) && Platform.isWindows()) {
                            System.gc();

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ex) {
                            }

                            deleteHeapDumpImpl(file, retries - 1);
                        } else {
                            ProfilerDialogs.displayError(Bundle.HeapWalkerManager_CannotDeleteHeapDumpMsg());
                        }
                    } else {
                        FileObject folder = FileUtil.toFileObject(file.getParentFile());
                        SnapshotsWindow.instance().refreshFolder(folder, true);
//                        if (ProfilerControlPanel2.hasDefault())
//                            ProfilerControlPanel2.getDefault().refreshSnapshotsList();
                    }
                }
            });
    }
}
