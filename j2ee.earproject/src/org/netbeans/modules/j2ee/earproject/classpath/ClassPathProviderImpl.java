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

package org.netbeans.modules.j2ee.earproject.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupportFactory;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;

/**
 * Defines the various class paths for an Enterprise Application project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider, PropertyChangeListener {
    
    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    
    private ClassPath runtimeClassPath = null;
    private ClassPath bootClassPath = null;
    
    private final Map<String,FileObject> dirCache = new HashMap<String,FileObject>();

    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        assert this.projectDirectory != null;
        this.evaluator = evaluator;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }
    
    private FileObject getDir(final String propname) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<FileObject>() {
            public FileObject run() {
                synchronized (ClassPathProviderImpl.this) {
                    FileObject fo = (FileObject) ClassPathProviderImpl.this.dirCache.get (propname);
                    if (fo == null ||  !fo.isValid()) {
                        String prop = evaluator.getProperty(propname);
                        if (prop != null) {
                            fo = helper.resolveFileObject(prop);
                            ClassPathProviderImpl.this.dirCache.put (propname, fo);
                        }
                    }
                    return fo;
                }
            }});
    }
    
    private FileObject getDistJar() {
        return getDir(EarProjectProperties.DIST_JAR);
    }
    
    private FileObject getBuildDir() {
        return getDir(EarProjectProperties.BUILD_DIR);
    }
    
    /**
     * Find what a given file represents.
     * @param file a file in the project
     * @return one of: <dl>
     *         <dt>0</dt> <dd>EAR build artifact</dd>
     *         <dt>-1</dt> <dd>something else</dd>
     *         </dl>
     */
    private int getType(FileObject file) {
        FileObject dir = getDistJar();
        if (dir != null && dir.equals(FileUtil.getArchiveFile(file))) {
            return 0;
        }
        dir = getBuildDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return 0;
        }
        return -1;
    }
    
    private synchronized ClassPath getRunTimeClasspath(FileObject file) {
        if (getType(file) == -1) {
            return null;
        }
        
        if (runtimeClassPath == null) {
            //XXX : It should return a classpath for run.classpath property, but
            // the run.classpath property was removed from the webproject in the past
            // and I'm a little lazy to return it back in the code:)). In this moment
            // the run classpath equals to the debug classpath. If the debug classpath
            // will be different from the run classpath, then the run classpath should
            // be returned back.
            runtimeClassPath = ClassPathFactory.createClassPath(
                ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                projectDirectory, evaluator, new String[] {"debug.classpath", EarProjectProperties.J2EE_PLATFORM_CLASSPATH})); // NOI18N
        }
        return runtimeClassPath;
    }
    
    private synchronized ClassPath getBootClassPath() {
        if (bootClassPath == null) {
            bootClassPath = ClassPathFactory.createClassPath(ClassPathSupportFactory.createBootClassPathImplementation(evaluator));
        }
        return bootClassPath;
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.EXECUTE)) {
            return getRunTimeClasspath(file);
        } else if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else {
            return null;
        }
    }
    
    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return new ClassPath[]{getBootClassPath()};
        }
        assert false;
        return null;
    }
    
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        dirCache.remove(evt.getPropertyName());
    }
    
}

