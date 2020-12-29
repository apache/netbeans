/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.project2.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.python.project2.PythonProject2;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

public final class CompilePathImplementation implements ClassPathImplementation, PropertyChangeListener, Runnable {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final File projectFolder;
    private List<PathResourceImplementation> resources;
    private AtomicBoolean dirty = new AtomicBoolean();

    CompilePathImplementation(final PythonProject2 project) {
        assert project != null;
        FileObject fo = project.getProjectDirectory();
        assert fo != null;
        this.projectFolder = FileUtil.toFile(fo);
        assert projectFolder != null;
        this.resources = this.getPath();
    }

    @Override
    public synchronized List<PathResourceImplementation> getResources() {
        assert this.resources != null;
        return this.resources;
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);
        support.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
//        if (prop != null && !PythonProjectProperties.PYTHON_LIB_PATH.equals(evt.getPropertyName())) {
//            // Not interesting to us.
//            return;
//        }
        // Coalesce changes; can come in fast after huge CP changes (#47910):
        if (!dirty.getAndSet(true)) {
            ProjectManager.mutex().postReadRequest(this);
        }
    }

    @Override
    public void run() {
        dirty.set(false);
        List<PathResourceImplementation> newRoots = getPath();
        boolean fire = false;
        synchronized (this) {
            if (!this.resources.equals(newRoots)) {
                this.resources = newRoots;
                fire = true;
            }
        }
        if (fire) {
            support.firePropertyChange(PROP_RESOURCES, null, null);
        }
    }

    private List<PathResourceImplementation> getPath() {
        List<PathResourceImplementation> result = new ArrayList<>();

//        String prop = evaluator.getProperty(PythonProjectProperties.PYTHON_LIB_PATH);
//        if (prop != null) {
//            //todo: Use PropertyUtil
//            final StringTokenizer tokenizer = new StringTokenizer(prop, "|");   //NOI18N
//            while (tokenizer.hasMoreTokens()) {
//                String piece = tokenizer.nextToken();
//                File f = PropertyUtils.resolveFile(this.projectFolder, piece);
//                URL entry = FileUtil.urlForArchiveOrDir(f);
//                if (entry != null) {
//                    result.add(ClassPathSupport.createResource(entry));
//                } else {
//                    Logger.getLogger(CompilePathImplementation.class.getName()).warning(f + " does not look like a valid archive file");
//                }
//            }
//        }
        return Collections.unmodifiableList(result);
    }
}
