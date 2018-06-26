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

package org.netbeans.modules.groovy.grailsproject.classpath;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @todo runtime classpath
 *
 * Defines the various class paths for a EJB project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider {

    private final GrailsProject project;
    private final SourceRoots sourceRoots;
    private final SourceRoots testSourceRoots;

    /**
     * Cache for classpaths:
     * <dl>
     *     <dt>0</dt> <dd>sources classpath</dd>
     *     <dt>1</dt> <dd>test sources classpath</dd>
     *     <dt>2</dt> <dd>sources compile classpath</dd>
     *     <dt>3</dt> <dd>test sources compile classpath</dd>
     *     <dt>4</dt> <dd>sources and built sources run classpath</dd>
     *     <dt>5</dt> <dd>test sources and built test sources run classpath</dd>
     *     <dt>6</dt> <dd>XXX: todo</dd>
     *     <dt>7</dt> <dd>boot classpath</dd>
     *     <dt>8</dt> <dd>J2EE platform classpath</dd>
     * </dl>
     */
    private final ClassPath[] cache = new ClassPath[9];

    private final Map<String, FileObject> dirCache = new HashMap<String, FileObject>();

    public ClassPathProviderImpl(SourceRoots sourceRoots, SourceRoots testSourceRoots, GrailsProject project) {
        this.project = project;
        this.sourceRoots = sourceRoots;
        this.testSourceRoots = testSourceRoots;
    }


    private synchronized FileObject getDir(String propname) {
        FileObject fo = this.dirCache.get(propname);
        return fo;
    }

    private FileObject[] getPrimarySrcPath() {
        return this.sourceRoots.getRoots();
    }

    private FileObject[] getTestSrcDir() {
         return this.testSourceRoots.getRoots();
    }

    private FileObject getBuildClassesDir() {
        return getDir("build.classes.dir");    //NOI18N
    }

    private FileObject getBuildJar() {
        return getDir("dist.jar");            //NOI18N
    }

    private FileObject getBuildTestClassesDir() {
        return getDir("build.test.classes.dir"); // NOI18N
    }

    /**
     * Find what a given file represents.
     * @param file a file in the project
     * @return one of: <dl>
     *         <dt>0</dt> <dd>normal source</dd>
     *         <dt>1</dt> <dd>test source</dd>
     *         <dt>2</dt> <dd>built class (unpacked)</dd>
     *         <dt>3</dt> <dd>built test class</dd>
     *         <dt>4</dt> <dd>built class (in dist JAR)</dd>
     *         <dt>-1</dt> <dd>something else</dd>
     *         </dl>
     */
    private int getType(FileObject file) {
        FileObject[] srcPath = getPrimarySrcPath();
        for (int i=0; i < srcPath.length; i++) {
            FileObject root = srcPath[i];
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return 0;
            }
        }
        srcPath = getTestSrcDir();
        for (int i=0; i< srcPath.length; i++) {
            FileObject root = srcPath[i];
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return 1;
            }
        }
        FileObject dir = getBuildClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return 2;
        }
        dir = getBuildJar();
        if (dir != null && (dir.equals(file))) {     //TODO: When MasterFs check also isParentOf
            return 4;
        }
        dir = getBuildTestClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir,file))) {
            return 3;
        }
        return -1;
    }

    private ClassPath getCompileTimeClasspath(FileObject file) {
        int type = getType(file);
        return this.getCompileTimeClasspath(type);
    }

    private synchronized ClassPath getCompileTimeClasspath(int type) {
        if (type < 0 || type > 1) {
            // Not a source file.
            return null;
        }
        ClassPath cp = cache[2+type];
        if ( cp == null) {
            cp = ClassPathFactory.createClassPath(ProjectClassPathImplementation.forProject(project));
            cache[2 + type] = cp;
        }
        return cp;
    }

    private synchronized ClassPath getRunTimeClasspath(FileObject file) {
        int type = getType(file);
        if (type < 0 || type > 4) {
            return null;
        } else if (type > 1) {
            type -= 2;
        }

        ClassPath cp = cache[4+type];
        return cp;
    }

    private ClassPath getSourcepath(FileObject file) {
        int type = getType(file);
        return this.getSourcepath(type);
    }

    private synchronized ClassPath getSourcepath(int type) {
        if (type < 0 || type > 1) {
            return null;
        }
        ClassPath cp = cache[type];
        if (cp == null) {
            switch (type) {
                case 0:
                    cp = ClassPathFactory.createClassPath(
                            SourcePathImplementation.forProject(project, this.sourceRoots));
                    break;
                case 1:
                    cp = ClassPathFactory.createClassPath(
                            SourcePathImplementation.forProject(project, this.testSourceRoots));
                    break;
            }
        }
        cache[type] = cp;
        return cp;
    }

    private synchronized ClassPath getBootClassPath() {
        ClassPath cp = cache[7];
        if (cp == null) {
            cp = ClassPathFactory.createClassPath(BootClassPathImplementation.forProject(project));
            cache[7] = cp;
        }
        return cp;
    }

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.COMPILE)) {
            return getCompileTimeClasspath(file);
        } else if (type.equals(ClassPath.EXECUTE)) {
            return getRunTimeClasspath(file);
        } else if (type.equals(ClassPath.SOURCE)) {
            return getSourcepath(file);
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
        if (ClassPath.COMPILE.equals(type)) {
            ClassPath[] l = new ClassPath[2];
            l[0] = getCompileTimeClasspath(0);
            l[1] = getCompileTimeClasspath(1);
            return l;
        }
        if (ClassPath.SOURCE.equals(type)) {
            ClassPath[] l = new ClassPath[2];
            l[0] = getSourcepath(0);
            l[1] = getSourcepath(1);
            return l;
        }
        assert false;
        return null;
    }

    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots).
     */
    public ClassPath getProjectSourcesClassPath(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return getBootClassPath();
        }
        if (ClassPath.COMPILE.equals(type)) {
            return getCompileTimeClasspath(0);
        }
        if (ClassPath.SOURCE.equals(type)) {
            return getSourcepath(0);
        }
        assert false;
        return null;
    }
}

