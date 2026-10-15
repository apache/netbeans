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

package org.netbeans.modules.profiler.heapwalk;

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.lib.profiler.heap.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.profiler.ResultsManager;
import org.netbeans.modules.profiler.api.ProfilerStorage;
import org.netbeans.modules.profiler.heapwalk.ui.HeapWalkerUI;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ClassesListController_HeapWalkerDefaultName=HeapWalker",
    "ClassesListController_LoadingDumpMsg=Loading Heap Dump..."
})
public class HeapWalker {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private File heapDumpFile;
    private HeapFragmentWalker mainHeapWalker;
//    private HeapWalkerUI heapWalkerUI;
    private TopComponent heapWalkerUI;
    private Lookup.Provider heapDumpProject;
    private String heapWalkerName;
    private final int segment;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Constructors ----------------------------------------------------------
    public HeapWalker(Heap heap) {
        this(heap, 0);
    }
    
    private HeapWalker(Heap heap, int segment) {
        this.segment = segment;
        heapWalkerName = Bundle.ClassesListController_HeapWalkerDefaultName();
        createMainFragment(heap, segment);
        
//        computeRetainedSizes();
    }

    public HeapWalker(File heapFile) throws FileNotFoundException, IOException {
        this(heapFile, 0);
    }

    HeapWalker(File heapFile, int segment) throws FileNotFoundException, IOException {
        this(createHeap(heapFile, segment), segment);

        heapDumpFile = heapFile;
        heapDumpProject = computeHeapDumpProject(heapDumpFile);

        String fileName = heapDumpFile.getName();
        int dotIndex = fileName.lastIndexOf('.'); // NOI18N
        if (dotIndex > 0 && dotIndex <= fileName.length() - 2)
            fileName = fileName.substring(0, dotIndex);
        heapWalkerName = ResultsManager.getDefault().getHeapDumpDisplayName(fileName);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public File getHeapDumpFile() {
        return heapDumpFile;
    }

    public int getHeapDumpSegment() {
        return segment;
    }

    public Lookup.Provider getHeapDumpProject() {
        return heapDumpProject;
    }

    // --- Internal interface ----------------------------------------------------
    public HeapFragmentWalker getMainHeapWalker() {
        return mainHeapWalker;
    }

    public String getName() {
        return heapWalkerName;
    }

    // --- Public interface ------------------------------------------------------
    public void open() {
        //    SwingUtilities.invokeLater(new Runnable() {
        //      public void run() {
        //        getTopComponent().open();
        // //        getTopComponent().requestActive(); // For some reason steals focus from Dump Heap button in ProfilerControlPanel2 and causes http://www.netbeans.org/issues/show_bug.cgi?id=92425
        //        getTopComponent().requestVisible(); // Workaround for the above problem
        //      }
        //    });
        HeapWalkerManager.getDefault().openHeapWalker(this);
    }

    public TopComponent getTopComponent() {
        if (heapWalkerUI == null) {
            heapWalkerUI = new HeapWalkerUI(this);
        }

        return heapWalkerUI;
    }

    void createMainFragment(Heap heap, int segment) {
        mainHeapWalker = new HeapFragmentWalker(heap, segment, this, true);
    }

    void createReachableFragment(Instance instance) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // TODO: Open new tab or select existing one
                }
            });
    }

    void createRetainedFragment(Instance instance) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // TODO: Open new tab or select existing one
                }
            });
    }
    
    private void computeRetainedSizes() {
        List<JavaClass> classes = mainHeapWalker.getHeapFragment().getAllClasses();
        if (classes.size() > 0) {
            ProgressHandle pd = ProgressHandle.createHandle(Bundle.HeapFragmentWalker_ComputingRetainedMsg());
            pd.start();
            classes.get(0).getRetainedSizeByClass();
            pd.finish();
        }
    }

    // --- Private implementation ------------------------------------------------
    private static Lookup.Provider computeHeapDumpProject(File heapDumpFile) {
        if (heapDumpFile == null) {
            return null;
        }

        File heapDumpDir = heapDumpFile.getParentFile();

        if (heapDumpDir == null) {
            return null;
        }

        FileObject heapDumpDirObj = FileUtil.toFileObject(heapDumpDir);

        if ((heapDumpDirObj == null) || !heapDumpDirObj.isValid()) {
            return null;
        }

        return ProfilerStorage.getProjectFromFolder(heapDumpDirObj);
    }

    private static Heap createHeap(File heapFile, int segment) throws FileNotFoundException, IOException {
        ProgressHandle pHandle = null;

        try {
            pHandle = ProgressHandle.createHandle(Bundle.ClassesListController_LoadingDumpMsg());
            pHandle.setInitialDelay(0);
            pHandle.start(HeapProgress.PROGRESS_MAX*2);
            
            setProgress(pHandle,0);
            Heap heap = HeapFactory.createHeap(heapFile, segment);
            setProgress(pHandle,HeapProgress.PROGRESS_MAX);
            heap.getSummary(); // Precompute HeapSummary within progress

            return heap;
        } finally {
            if (pHandle != null) {
                pHandle.finish();
            }
        }
    }

    private static void setProgress(final ProgressHandle pHandle, final int offset) {
        final BoundedRangeModel progress = HeapProgress.getProgress();
        progress.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                pHandle.progress(progress.getValue()+offset);
            }
        });
    }
}
