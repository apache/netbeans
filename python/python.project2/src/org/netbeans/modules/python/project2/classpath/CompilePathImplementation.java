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
