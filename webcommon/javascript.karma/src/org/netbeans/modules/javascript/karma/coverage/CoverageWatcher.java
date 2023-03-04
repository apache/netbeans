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

package org.netbeans.modules.javascript.karma.coverage;

import java.io.File;
import java.util.Enumeration;
import org.netbeans.modules.web.clientproject.api.jstesting.Coverage;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

public final class CoverageWatcher {

    static final String COVERAGE_FILENAME = "clover.xml"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(CoverageWatcher.class);

    private final Coverage coverage;
    private final File sourceDir;
    private final File coverageDir;
    private final FileChangeListener fileChangeListener = new FileChangeListenerImpl();

    private volatile boolean coverageProcessing = false;


    public CoverageWatcher(Coverage coverage, File sourceDir, File coverageDir) {
        assert coverage != null;
        assert sourceDir.isDirectory() : sourceDir;
        assert coverageDir != null;
        this.coverage = coverage;
        this.sourceDir = sourceDir;
        this.coverageDir = coverageDir;
    }

    public void start() {
        FileUtil.addRecursiveListener(fileChangeListener, coverageDir);
    }

    public void stop() {
        FileUtil.removeRecursiveListener(fileChangeListener, coverageDir);
    }

    void process(final File logFile) {
        assert logFile.isFile();
        if (coverageProcessing) {
            return;
        }
        coverageProcessing = true;
        RP.post(new Runnable() {
            @Override
            public void run() {
                processInternal(logFile);
            }
        });
    }

    void processInternal(File logFile) {
        try {
            if (coverage.isEnabled()) {
                new CoverageProcessor(coverage, sourceDir, logFile).process();
            }
        } finally {
            coverageProcessing = false;
        }
    }

    //~ Inner classes

    private final class FileChangeListenerImpl extends FileChangeAdapter {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            processFolder(fe.getFile());
        }

        @Override
        public void fileChanged(FileEvent fe) {
            processFile(fe.getFile());
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            processFile(fe.getFile());
        }

        private void processFolder(FileObject folder) {
            assert folder.isFolder() : folder;
            // folder newly created -> try to locate any clover.xml in it
            Enumeration<? extends FileObject> children = folder.getChildren(true);
            while (children.hasMoreElements()) {
                FileObject child = children.nextElement();
                if (child.isData()) {
                    processFile(child);
                }
            }
        }

        private void processFile(FileObject file) {
            assert file.isData() : file;
            if (COVERAGE_FILENAME.equals(file.getNameExt())) {
                process(FileUtil.toFile(file));
            }
        }

    }

}
