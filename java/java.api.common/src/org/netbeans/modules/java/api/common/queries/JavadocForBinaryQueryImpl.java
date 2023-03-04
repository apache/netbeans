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
package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;
import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Finds Javadoc (if it is built) corresponding to binaries in a project.
 * @author David Konecny, Jesse Glick
 */
class JavadocForBinaryQueryImpl implements JavadocForBinaryQueryImplementation {

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final String[] binaryProperties;

    public JavadocForBinaryQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator, String[] binaryProperties) {
        assert helper != null;
        assert evaluator != null;
        assert binaryProperties != null && binaryProperties.length > 0;

        this.helper = helper;
        this.evaluator = evaluator;
        this.binaryProperties = binaryProperties;
    }

    public JavadocForBinaryQuery.Result findJavadoc(final URL binaryRoot) {

        class Result implements JavadocForBinaryQuery.Result, PropertyChangeListener  {

            private final ChangeSupport changeSupport = new ChangeSupport(this);
            private URL[] result;
            private long eventId;

            public Result() {
                JavadocForBinaryQueryImpl.this.evaluator.addPropertyChangeListener(
                        WeakListeners.propertyChange(this, JavadocForBinaryQueryImpl.this.evaluator));
            }

            public URL[] getRoots() {
                long lEventId;
                synchronized (this) {
                    if (this.result != null) {
                        return this.result;
                    }
                    lEventId = eventId;
                }
                URL[] lResult;
                String javadocDir = evaluator.getProperty(ProjectProperties.DIST_JAVADOC_DIR);
                if (javadocDir != null) {
                    File f = helper.resolveFile(javadocDir);
                    try {
                        URL url = Utilities.toURI(f).toURL();
                        if (!f.exists()) {
                            assert !url.toExternalForm().endsWith("/") : f; // NOI18N
                            url = new URL(url.toExternalForm() + "/"); // NOI18N
                        }
                        lResult = new URL[] {url};
                    } catch (MalformedURLException e) {
                        lResult = new URL[0];
                        Exceptions.printStackTrace(e);
                    }
                } else {
                    lResult = new URL[0];
                }
                synchronized (this) {
                    if (lEventId == eventId) {
                        if (this.result == null) {
                            this.result = lResult;
                        }
                        return this.result;
                    }
                    return lResult;
                }
            }

            public void addChangeListener(final ChangeListener l) {
                changeSupport.addChangeListener(l);
            }

            public void removeChangeListener(final ChangeListener l) {
                changeSupport.removeChangeListener(l);
            }

            public void propertyChange(final PropertyChangeEvent event) {
                if (ProjectProperties.DIST_JAVADOC_DIR.equals(event.getPropertyName())) {
                    synchronized (this) {
                        result = null;
                        eventId++;
                    }
                    this.changeSupport.fireChange();
                }
            }
        }
        for (String property : binaryProperties) {
            if (isRootOwner(binaryRoot, property)) {
                return new Result();
            }
        }
        return null;
    }

    private boolean isRootOwner(URL binaryRoot, String binaryProperty) {
        try {
            if (FileUtil.getArchiveFile(binaryRoot) != null) {
                binaryRoot = FileUtil.getArchiveFile(binaryRoot);
                // XXX check whether this is really the root
            }
            String outDir = evaluator.getProperty(binaryProperty);
            if (outDir != null) {
                File f = helper.resolveFile(outDir);
                URL url = Utilities.toURI(f).toURL();
                if (!f.exists() && !f.getPath().toLowerCase().endsWith(".jar")) { // NOI18N
                    assert !url.toExternalForm().endsWith("/") : f; // NOI18N
                    url = new URL(url.toExternalForm() + "/"); // NOI18N
                }
                return url.equals(binaryRoot)
                        || binaryRoot.toExternalForm().startsWith(url.toExternalForm());
            }
        } catch (MalformedURLException malformedURL) {
            Exceptions.printStackTrace(malformedURL);
        }
        return false;
    }
}
