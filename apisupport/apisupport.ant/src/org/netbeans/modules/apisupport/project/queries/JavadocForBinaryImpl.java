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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.JavadocForBinaryQuery.Result;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleType;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

/**
 * Defines Javadoc locations for built modules with built javadoc.
 * @author Jesse Glick
 */
public final class JavadocForBinaryImpl implements JavadocForBinaryQueryImplementation {
    
    private static final String NB_ALL_INFIX = "nbbuild" + File.separatorChar + "build" + File.separatorChar + "javadoc" + File.separatorChar; // NOI18N
    private static final String EXT_INFIX = "build" + File.separatorChar + "javadoc" + File.separatorChar; // NOI18N
    
    /** Configurable for the unit test, since it is too cumbersome to create fake Javadoc in all the right places. */
    static boolean ignoreNonexistentRoots = true;
    
    private final NbModuleProject project;
    
    public JavadocForBinaryImpl(NbModuleProject project) {
        this.project = project;
    }

    public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
        if (!binaryRoot.equals(FileUtil.urlForArchiveOrDir(project.getModuleJarLocation()))) {
            return findForCPExt(binaryRoot);
        }
        String cnb = project.getCodeNameBase();
        if (cnb == null) { // #115521
            return null;
        }
        String cnbdashes = cnb.replace('.', '-');
        try {
            final List<URL> candidates = new ArrayList<URL>();
            NbPlatform platform = project.getPlatform(false);
            if (platform == null) {
                return null;
            }
            for (URL root : platform.getJavadocRoots()) {
                candidates.add(new URL(root, cnbdashes + "/")); // NOI18N
            }
            File dir;
            NbModuleType type = project.getModuleType();
            if (type == NbModuleType.NETBEANS_ORG) {
                dir = project.getNbrootFile(NB_ALL_INFIX + cnbdashes);
            } else {
                // XXX should evaluate and use ${build.dir}/javadoc instead
                dir = new File(project.getProjectDirectoryFile(), EXT_INFIX + cnbdashes);
            }
            if (dir != null) { // #118491
                candidates.add(FileUtil.urlForArchiveOrDir(dir));
            }
            if (ignoreNonexistentRoots) {
                Iterator<URL> it = candidates.iterator();
                while (it.hasNext()) {
                    URL u = it.next();
                    if (URLMapper.findFileObject(u) == null) {
                        it.remove();
                    }
                }
            }
            return new R(candidates.toArray(new URL[0]));
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Find Javadoc roots for classpath extensions ("wrapped" JARs) of the project
     * added by naming convention <tt>&lt;jar name&gt;-javadoc(.zip)</tt>
     * See issue #66275
     * @param binaryRoot
     * @return
     */
    private Result findForCPExt(URL binaryRoot) {
        URL jar = FileUtil.getArchiveFile(binaryRoot);
        if (jar == null)
            return null;    // not a class-path-extension
        File binaryRootF = Utilities.toFile(URI.create(jar.toExternalForm()));
        // XXX this will only work for modules following regular naming conventions:
        String n = binaryRootF.getName();
        if (!n.endsWith(".jar")) { // NOI18N
            // ignore
            return null;
        }
        // convention-over-cfg per mkleint's suggestion: <jarname>-javadoc(.zip) folder or ZIP
        File jFolder = new File(binaryRootF.getParentFile(), 
                n.substring(0, n.length() - ".jar".length()) + "-javadoc");
        if (jFolder.isDirectory()) {
                return new R(new URL[]{FileUtil.urlForArchiveOrDir(jFolder)});
        } else {
            File jZip = new File(jFolder.getAbsolutePath() + ".zip");
            if (jZip.isFile()) {
                return new R(new URL[]{FileUtil.urlForArchiveOrDir(jZip)});
            }
        }
        return null;
    }

    private static final class R implements JavadocForBinaryQuery.Result {
        private final URL[] roots;
        public R(URL[] roots) {
            this.roots = roots;
        }
        public URL[] getRoots() {
            return roots;
        }
        public void addChangeListener(ChangeListener l) {}
        public void removeChangeListener(ChangeListener l) {}
    }

}
