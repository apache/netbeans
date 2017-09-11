/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex.Action;


/**
 * Default implementation of {@link FileBuiltQueryImplementation}.
 * @author Jesse Glick, Tomas Zezula
 */
final class FileBuiltQueryImpl implements FileBuiltQueryImplementation, PropertyChangeListener {

    private FileBuiltQueryImplementation delegate;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private File buildGeneratedDir = null;
    private final FileChangeListener buildGeneratedDirListener = new FileChangeAdapter() {
        public @Override void fileFolderCreated(FileEvent fe) {
            invalidate();
        }
        public @Override void fileDeleted(FileEvent fe) {
            invalidate();
        }
        public @Override void fileRenamed(FileRenameEvent fe) {
            invalidate();
        }
    };

    FileBuiltQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots,
            SourceRoots testRoots) {
        assert helper != null;
        assert evaluator != null;
        assert sourceRoots != null;
        assert testRoots != null;

        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testRoots = testRoots;
        this.sourceRoots.addPropertyChangeListener(this);
        this.testRoots.addPropertyChangeListener(this);
    }

    public FileBuiltQuery.Status getStatus(final FileObject file) {
        return ProjectManager.mutex().readAccess(new Action<FileBuiltQuery.Status>() {
            public FileBuiltQuery.Status run() {
                return getStatusImpl(file);
            }
        });
    }

    private synchronized FileBuiltQuery.Status getStatusImpl(FileObject file) {
        if (delegate == null) {
            delegate = createDelegate();
        }
        return delegate.getStatus(file);
    }


    private FileBuiltQueryImplementation createDelegate() {
        List<String> from = new ArrayList<String>();
        List<String> to = new ArrayList<String>();
        for (String r : sourceRoots.getRootProperties()) {
            from.add("${" + r + "}/*.java"); // NOI18N
            to.add("${build.classes.dir}/*.class"); // NOI18N
        }
        for (String r : testRoots.getRootProperties()) {
            from.add("${" + r + "}/*.java"); // NOI18N
            to.add("${build.test.classes.dir}/*.class"); // NOI18N
        }
        String buildGeneratedDirS = evaluator.getProperty("build.generated.sources.dir"); // NOI18N
        if (buildGeneratedDirS != null) { // #105645
            File _buildGeneratedDir = helper.resolveFile(buildGeneratedDirS);
            if (!_buildGeneratedDir.equals(buildGeneratedDir)) {
                if (buildGeneratedDir != null) {
                    FileUtil.removeFileChangeListener(buildGeneratedDirListener, buildGeneratedDir);
                }
                buildGeneratedDir = _buildGeneratedDir;
                FileUtil.addFileChangeListener(buildGeneratedDirListener, buildGeneratedDir);
            }
            if (buildGeneratedDir.isDirectory()) {
                for (File root : buildGeneratedDir.listFiles()) {
                    if (!root.isDirectory()) {
                        continue;
                    }
                    from.add(root + "/*.java"); // NOI18N
                    to.add("${build.classes.dir}/*.class"); // NOI18N
                }
            }
        }
        return helper.createGlobFileBuiltQuery(evaluator,
                from.toArray(new String[from.size()]),
                to.toArray(new String[to.size()]));
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (SourceRoots.PROP_ROOT_PROPERTIES.equals(evt.getPropertyName())) {
            invalidate();
        }
    }

    private synchronized void invalidate() {
        delegate = null;
        // XXX: what to do with already returned Statuses
    }

}
