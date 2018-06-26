/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
