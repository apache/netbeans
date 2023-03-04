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
package org.netbeans.modules.j2ee.clientproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.Collection;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import org.openide.util.RequestProcessor;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.WeakListeners;

class MainClassUpdater extends FileChangeAdapter implements PropertyChangeListener {

    private static RequestProcessor RP = new RequestProcessor();

    private final Project project;
    private final PropertyEvaluator eval;
    private final UpdateHelper helper;
    private final ClassPath sourcePath;
    private final String mainClassPropName;
    private FileObject current;
    private FileChangeListener listener;
    
    /** Creates a new instance of MainClassUpdater */
    public MainClassUpdater(final Project project, final PropertyEvaluator eval,
        final UpdateHelper helper, final ClassPath sourcePath, final String mainClassPropName) {
        assert project != null;
        assert eval != null;
        assert helper != null;
        assert sourcePath != null;
        assert mainClassPropName != null;
        this.project = project;
        this.eval = eval;
        this.helper = helper;
        this.sourcePath = sourcePath;
        this.mainClassPropName = mainClassPropName;        
        this.eval.addPropertyChangeListener(this);
        this.addFileChangeListener ();
    }
    
    public synchronized void unregister () {
        if (current != null && listener != null) {
            current.removeFileChangeListener(listener);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (this.mainClassPropName.equals(evt.getPropertyName())) {
            //Go out of the ProjectManager.MUTEX, see #118722
            RP.post(new Runnable () {
                public void run() {
                    MainClassUpdater.this.addFileChangeListener ();
                }
            });            
        }
    }
    
    @Override
    public void fileRenamed (final FileRenameEvent evt) {
        if (!project.getProjectDirectory().isValid()) {
            return;
        }
        final FileObject _current;
        synchronized (this) {
            _current = this.current;
        }
        if (evt.getFile() == _current) {
            Runnable r = new Runnable () {
                public void run () {  
                    try {
                        final String oldMainClass = ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<String>() {
                            public String run() throws Exception {
                                return eval.getProperty(mainClassPropName);
                            }
                        });

                        Collection<ElementHandle<TypeElement>> main = SourceUtils.getMainClasses(_current);
                        String newMainClass = null;
                        if (!main.isEmpty()) {
                            ElementHandle<TypeElement> mainHandle = main.iterator().next();
                            newMainClass = mainHandle.getQualifiedName();
                        }                    
                        if (newMainClass != null && !newMainClass.equals(oldMainClass) && helper.requestUpdate()) {
                            final String newMainClassFinal = newMainClass;
                            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                                public Void run() throws Exception {                                                                                    
                                    EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                    props.put (mainClassPropName, newMainClassFinal);
                                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                    ProjectManager.getDefault().saveProject (project);
                                    return null;
                                }
                            });
                        }
                    } catch (IOException e) {
                        Exceptions.printStackTrace(e);
                    }
                    catch (MutexException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                r.run();
            }
            else {
                SwingUtilities.invokeLater(r);
            }
        }
    }
    
    private void addFileChangeListener () {
        synchronized (MainClassUpdater.this) {
            if (current != null && listener != null) {
                current.removeFileChangeListener(listener);
                current = null;
                listener = null;
            }            
        }
        final String mainClassName = MainClassUpdater.this.eval.getProperty(mainClassPropName);
        if (mainClassName != null) {
            try {
                FileObject[] roots = sourcePath.getRoots();
                if (roots.length>0) {
                    ClassPath bootCp = ClassPath.getClassPath(roots[0], ClassPath.BOOT);
                    ClassPath compileCp = ClassPath.getClassPath(roots[0], ClassPath.COMPILE);
                    final ClasspathInfo cpInfo = ClasspathInfo.create(bootCp, compileCp, sourcePath);
                    JavaSource js = JavaSource.create(cpInfo);
                    js.runWhenScanFinished(new Task<CompilationController>() {

                        public void run(CompilationController c) throws Exception {
                            TypeElement te = c.getElements().getTypeElement(mainClassName);
                             if (te != null) {
                                synchronized (MainClassUpdater.this) {
                                    current = SourceUtils.getFile(te, cpInfo);
                                    listener = WeakListeners.create(FileChangeListener.class, MainClassUpdater.this, current);
                                    if (current != null && sourcePath.contains(current)) {
                                        current.addFileChangeListener(listener);
                                    }
                                }
                            }                            
                        }

                    }, true);
                }
            } catch (IOException ioe) {
            }
        }        
    }

}
