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
package org.netbeans.modules.profiler.heapwalk.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.profiler.ResultsManager;
import org.netbeans.modules.profiler.heapwalk.HeapWalkerManager;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Opens the Heap Walker
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "OpenHeapWalkerAction_ActionName=Load Heap D&ump...",
    "OpenHeapWalkerAction_DialogCaption=Open Heap Dump File"
})
public final class OpenHeapWalkerAction implements ActionListener {
    private static File importDir;

    public void actionPerformed(ActionEvent e) {
        final File heapDumpFile = getHeapDumpFile();
        BrowserUtils.performTask(new Runnable() {
                public void run() {
                    if (heapDumpFile != null) {
                        HeapWalkerManager.getDefault().openHeapWalker(heapDumpFile);
                    }
                }
            });
    }
    private static File getHeapDumpFile() {
        JFileChooser chooser = new JFileChooser();

        if (importDir != null) {
            chooser.setCurrentDirectory(importDir);
        }

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());
        chooser.addChoosableFileFilter(new FileFilterImpl());
        chooser.setDialogTitle(
            Bundle.OpenHeapWalkerAction_DialogCaption()
        );

        if (chooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
            importDir = chooser.getCurrentDirectory();

            return chooser.getSelectedFile();
        }

        return null;
    }

    private static class FileFilterImpl extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            return ResultsManager.checkHprofFile(f);
        }

        @NbBundle.Messages({
            "FileDescription=Heap Dumps (*.hprof, *.bin, *.*)"
        })
        @Override
        public String getDescription() {
            return Bundle.FileDescription();
        }
    }
}
