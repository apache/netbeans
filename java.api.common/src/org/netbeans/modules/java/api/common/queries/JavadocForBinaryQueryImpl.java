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
