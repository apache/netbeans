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
package org.netbeans.modules.ide.dashboard;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.modules.openfile.OpenFile;
import org.netbeans.modules.openfile.RecentFiles;
import org.netbeans.spi.dashboard.DashboardDisplayer;
import org.netbeans.spi.dashboard.DashboardWidget;
import org.netbeans.spi.dashboard.WidgetElement;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Actions;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * List of recent files.
 */
@Messages({
    "TITLE_RecentFiles=Recent Files",
    "LBL_NoRecentFiles=<no recent files>",
    "LBL_OpenRecentFile=Open Recent File"
})
public class RecentFilesWidget implements DashboardWidget {

    private static final RequestProcessor RP = new RequestProcessor("RecentFiles");

    private static final int MAX_FILES = 5;

    private record FileInfo(File file, Icon icon) {

    }

    private final List<WidgetElement> elements;
    private final List<FileInfo> files;
    private final Set<DashboardDisplayer.Panel> active;
    private final PropertyChangeListener filesListener;

    private final Action newFile;
    private final Action openFile;

    public RecentFilesWidget() {
        elements = new ArrayList<>();
        files = new ArrayList<>();
        active = new HashSet<>();
        filesListener = e -> {
            loadFiles();
        };
        Action newFileOriginal = Actions.forID("Project", "org.netbeans.modules.project.ui.NewFile");
        if (newFileOriginal != null) {
            newFile = new FilesDelegateAction(newFileOriginal);
        } else {
            newFile = null;
        }
        Action openFileOriginal = Actions.forID("System", "org.netbeans.modules.openfile.OpenFileAction");
        if (openFileOriginal != null) {
            openFile = new FilesDelegateAction(openFileOriginal);
        } else {
            openFile = null;
        }
        buildElements();
        loadFiles();
    }

    @Override
    public String title(DashboardDisplayer.Panel panel) {
        return Bundle.TITLE_RecentFiles();
    }

    @Override
    public List<WidgetElement> elements(DashboardDisplayer.Panel panel) {
        return List.copyOf(elements);
    }

    @Override
    public void showing(DashboardDisplayer.Panel panel) {
        if (active.isEmpty()) {
            RecentFiles.addPropertyChangeListener(filesListener);
        }
        active.add(panel);
        panel.refresh();
        loadFiles();
    }

    @Override
    public void hidden(DashboardDisplayer.Panel panel) {
        active.remove(panel);
        if (active.isEmpty()) {
            RecentFiles.removePropertyChangeListener(filesListener);
        }
    }

    private void loadFiles() {
        RP.execute(() -> {
            List<FileInfo> files = new ArrayList<>(MAX_FILES);
            for (RecentFiles.HistoryItem item : RecentFiles.getRecentFiles()) {
                File file = new File(item.getPath());
                if (!file.exists()) {
                    continue;
                }
                Icon icon = item.getIcon();
                files.add(new FileInfo(file, icon));
                if (files.size() >= MAX_FILES) {
                    break;
                }
            }
            EventQueue.invokeLater(() -> {
                updateFilesList(files);
            });

        });

    }

    private void buildElements() {
        elements.clear();
        if (files.isEmpty()) {
            elements.add(WidgetElement.unavailable(Bundle.LBL_NoRecentFiles()));
        } else {
            for (FileInfo info : files) {
                elements.add(WidgetElement.actionLink(new OpenFileAction(info)));
            }
        }
        if (newFile != null || openFile != null) {
            elements.add(WidgetElement.separator());
        }
        if (newFile != null) {
            elements.add(WidgetElement.actionLink(newFile));
        }
        if (openFile != null) {
            elements.add(WidgetElement.actionLink(openFile));
        }
    }

    private void updateFilesList(List<FileInfo> files) {
        if (!this.files.equals(files)) {
            this.files.clear();
            this.files.addAll(files);
            buildElements();
            active.forEach(DashboardDisplayer.Panel::refresh);
        }
    }

    private static class FilesDelegateAction extends AbstractAction {

        private final Action delegate;

        private FilesDelegateAction(Action delegate) {
            super(Actions.cutAmpersand(String.valueOf(delegate.getValue(NAME)).replace("...", "")));
            this.delegate = delegate;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            delegate.actionPerformed(e);
        }

    }

    private static class OpenFileAction extends AbstractAction {

        private final FileInfo info;

        private OpenFileAction(FileInfo info) {
            super(info.file().getName(), info.icon());
            this.info = info;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            File nf = FileUtil.normalizeFile(info.file());
            String msg = OpenFile.open(FileUtil.toFileObject(nf), -1);
            if (msg != null) {
                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(msg));
            }
        }

    }

}
