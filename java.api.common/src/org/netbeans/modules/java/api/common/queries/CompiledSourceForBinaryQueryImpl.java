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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import org.netbeans.modules.java.api.common.SourceRoots;
import java.io.File;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import java.net.URL;
import java.net.MalformedURLException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Default implementation of {@link SourceForBinaryQueryImplementation}.
 * @author Jesse Glick, Tomas Zezula
 */
class CompiledSourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation {

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private final Map<URL, SourceForBinaryQuery.Result> cache = new HashMap<URL, SourceForBinaryQuery.Result>();
    private final String[] binaryProperties;
    private final String[] testBinaryProperties;

    public CompiledSourceForBinaryQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots srcRoots,
            SourceRoots testRoots, String[] binaryProperties, String[] testBinaryProperties) {
        assert helper != null;
        assert evaluator != null;
        assert srcRoots != null;
        assert binaryProperties != null && binaryProperties.length > 0;
        assert testRoots == null ? testBinaryProperties == null : (testBinaryProperties != null && testBinaryProperties.length > 0);
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = srcRoots;
        this.testRoots = testRoots;
        this.binaryProperties = binaryProperties;
        this.testBinaryProperties = testBinaryProperties;
    }

    @Override
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        if (FileUtil.getArchiveFile(binaryRoot) != null) {
            binaryRoot = FileUtil.getArchiveFile(binaryRoot);
            // XXX check whether this is really the root
        }
        SourceForBinaryQuery.Result res = cache.get(binaryRoot);
        if (res != null) {
            return res;
        }
        SourceRoots src = null;
        for (String property : binaryProperties) {
            if (hasSources(binaryRoot, property)) {
                src = sourceRoots;
                break;
            }
        }
        if (src == null) {
            if (testBinaryProperties != null) {
                for (String property : testBinaryProperties) {
                    if (hasSources(binaryRoot, property)) {
                        src = testRoots;
                        break;
                    }
                }
            }
        }
        if (src == null) {
            return null;
        }
        res = new Result(src, src == sourceRoots);
        cache.put(binaryRoot, res);
        return res;
    }

    private boolean hasSources(URL binaryRoot, String binaryProperty) {
        try {
            String outDir = evaluator.getProperty(binaryProperty);
            if (outDir != null) {
                File f = helper.resolveFile(outDir);
                URL url = Utilities.toURI(f).toURL();
                if (!f.exists() && !f.getPath().toLowerCase().endsWith(".jar")) { // NOI18N
                    // non-existing
                    assert !url.toExternalForm().endsWith("/") : f; // NOI18N
                    url = new URL(url.toExternalForm() + "/"); // NOI18N
                }
                if (url.equals(binaryRoot)) {
                    return true;
                }
            }
        } catch (MalformedURLException malformedURL) {
            Exceptions.printStackTrace(malformedURL);
        }
        return false;
    }

    private class Result implements SourceForBinaryQuery.Result, PropertyChangeListener {

        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private SourceRoots sourceRoots;
        private final boolean gensrc;

        @SuppressWarnings("LeakingThisInConstructor")
        public Result(SourceRoots sourceRoots, boolean gensrc) {
            this.sourceRoots = sourceRoots;
            this.sourceRoots.addPropertyChangeListener(this);
            this.gensrc = gensrc;
        }

        @Override
        public FileObject[] getRoots() {
            if (gensrc) { // #105645
                String buildGeneratedDirS = evaluator.getProperty("build.generated.sources.dir"); // NOI18N
                if (buildGeneratedDirS != null) {
		    final String apSourcesDirS = evaluator.getProperty(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT);
		    final FileObject apSourcesDir = apSourcesDirS != null ? helper.resolveFileObject(apSourcesDirS) : null;
                    FileObject buildGeneratedDir = helper.resolveFileObject(buildGeneratedDirS);
                    if (buildGeneratedDir != null) {
                        List<FileObject> roots = new ArrayList<FileObject>(Arrays.asList(sourceRoots.getRoots()));
                        for (FileObject root : buildGeneratedDir.getChildren()) {
			    if (root.equals(apSourcesDir)) {
				continue;
			    }
                            if (root.isFolder()) {
                                roots.add(root);
                            }
                        }
                        return roots.toArray(new FileObject[roots.size()]);
                    }
                }
            }
            return this.sourceRoots.getRoots(); // no need to cache it, SourceRoots does
        }

        @Override
        public void addChangeListener (ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener (ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (SourceRoots.PROP_ROOTS.equals(evt.getPropertyName())) {
                changeSupport.fireChange();
            }
        }
    }
}
