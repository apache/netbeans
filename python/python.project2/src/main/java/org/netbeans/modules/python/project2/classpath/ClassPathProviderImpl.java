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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.python.project2.classpath;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.python.project2.PythonProject2;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 * Defines various paths for the Python Project.
 * Based on the Ruby project, in turn based on the J2SE project.
 * Greatly simplified at the moment since Python projects don't have a Sources object,
 * and there's no separate source/test folders.
 * @author Tor Norbye
 * @author Tomas Zezula
 */
public final class ClassPathProviderImpl implements ClassPathProvider {
    private final PythonProject2 project;
    private final Sources sources;
    private final Map<Pair<String,Integer>,ClassPath> cache = new HashMap<>();

    public ClassPathProviderImpl(final PythonProject2 project, Sources sources) {
        assert project != null;
        this.project = project;
        this.sources = sources;
        assert this.sources != null;
    }

    private static final int MAX_TYPES = 3;
    /**
     * Find what a given file represents.
     * @param file a file in the project
     * @return one of: <dl>
     *         <dt>0</dt> <dd>normal source</dd>
     *         <dt>1</dt> <dd>test source</dd>
     *         <dt>2</dt> <dd>the project root</dd>
     *         <dt>-1</dt> <dd>something else</dd>
     *         </dl>
     */
    private int getType(FileObject file) {
        if (file == project.getProjectDirectory()) {
            return 2;
        }
        for (SourceGroup sourceGroup : sources.getSourceGroups(PythonProject2.SOURCES_TYPE_PYTHON)) {
            FileObject root = sourceGroup.getRootFolder();
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return 0;
            }
        }
        return -1;
    }

    private synchronized ClassPath getSourcepath(FileObject file) {
        int type = getType(file);
        return this.getSourcepath(type);
    }

    private ClassPath getSourcepath(int type) {
        if (type < 0 || type > MAX_TYPES) {
            return null;
        }
        final Pair<String,Integer> key = Pair.of(ClassPath.SOURCE, type);
        ClassPath cp = cache.get(key);
        if (cp == null) {
            switch (type) {
                case 0:
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation(sources));
                    break;
//                case 1:
//                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation(tests));
//                    break;
                case 2:
                    // Classpath for the "whole project" - for now just use the sources
                    // Used from the tasklist for example.
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation(sources));
                    break;

                default:
                    throw new UnsupportedOperationException("Only sources are available in the Python project at this point");
           }
           cache.put (key,cp);
        }
        return cp;
    }

    private synchronized ClassPath getBootClassPath() {        
        final Pair<String,Integer> key = Pair.of(ClassPath.BOOT, 0);
        ClassPath cp = cache.get(key);
        if (cp == null) {
            //todo: For now merge compile and platform class paths
            //under parsing api they should be separated
            final ClassPathImplementation boot = new BootClassPathImplementation(project);
            final ClassPathImplementation compile = new CompilePathImplementation(project);
            cp = ClassPathFactory.createClassPath(ClassPathSupport.createProxyClassPathImplementation(boot,compile));
           cache.put (key,cp);
        }        
        return cp;
    }

//    private synchronized ClassPath getCompileClassPath() {
//        final Pair<String,Integer> key = Pair.of(ClassPath.COMPILE, 0);
//        ClassPath cp = cache.get(key);
//        if (cp == null) {
//            cp = ClassPathFactory.createClassPath(new CompilePathImplementation(this.project));
//           cache.put (key,cp);
//        }
//        return cp;
//    }

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.SOURCE)) {
            return getSourcepath(file);
        } else if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else if (type.equals(ClassPath.COMPILE)) {
            // Bogus
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
        if (ClassPath.SOURCE.equals(type)) {
            ClassPath[] l = new ClassPath[1];
            l[0] = getSourcepath(0);
            return l;
        }
        return null;
    }

    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots). Valid types are BOOT, SOURCE and COMPILE.
     */
    public ClassPath getProjectSourcesClassPath(String type) {
        if (ClassPath.BOOT.equals(type)) {
             return getBootClassPath();
        }
        if (ClassPath.SOURCE.equals(type)) {
            return getSourcepath(0);
        }
        return null;
    }            
}
