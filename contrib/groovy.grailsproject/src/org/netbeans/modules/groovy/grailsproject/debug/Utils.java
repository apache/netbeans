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


package org.netbeans.modules.groovy.grailsproject.debug;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * various debugger related utility methods.
 * @author mkleint
 */
public class Utils {

    /** Creates a new instance of Utils */
    private Utils() {
    }

//    static MethodBreakpoint createBreakpoint(String stopClassName) {
//        MethodBreakpoint breakpoint = MethodBreakpoint.create(
//                stopClassName,
//                "*" //NOI18N
//                );
//        breakpoint.setHidden(true);
//        DebuggerManager.getDebuggerManager().addBreakpoint(breakpoint);
//        return breakpoint;
//    }

    public static File[] convertStringsToNormalizedFiles(Collection<String> strings) {
        File[] fos = new File[strings.size()];
        int index = 0;
        Iterator it = strings.iterator();
        while (it.hasNext()) {
            String str = (String)it.next();
            File fil = new File(str);
            fil = FileUtil.normalizeFile(fil);
            fos[index] = fil;
            index++;
        }
        return fos;
    }

    static ClassPath createSourcePath(Project project) {
        GrailsProject grailsProject = project.getLookup().lookup(GrailsProject.class);

        Set<URL> urls = new HashSet<URL>();
        urls.addAll(grailsProject.getSourceRoots().getRootURLs());
        // this is dup of above line in fact
        urls.addAll(grailsProject.getTestSourceRoots().getRootURLs());

        return ClassPathSupport.createClassPath(urls.toArray(new URL[0]));
    }

    static ClassPath createJDKSourcePath(Project nbproject) {
        GrailsProjectConfig config = nbproject.getLookup().lookup(GrailsProjectConfig.class);
        JavaPlatform jp = config.getJavaPlatform();
        if (jp == null) {
            jp = JavaPlatformManager.getDefault().getDefaultPlatform();
        }
        if (jp != null) {
            return jp.getSourceFolders();
        }
        return ClassPathSupport.createClassPath(new URL[0]);
    }

    private static ClassPath convertToClassPath(File[] roots) {
        List<URL> l = new ArrayList<URL>();
        for (int i = 0; i < roots.length; i++) {
            URL url = Utils.fileToURL(roots[i]);
            l.add(url);
        }
        URL[] urls = l.toArray(new URL[0]);
        return ClassPathSupport.createClassPath(urls);
    }

    /**
     * This method uses SourceForBinaryQuery to find sources for each
     * path item and returns them as ClassPath instance. All path items for which
     * the sources were not found are omitted.
     *
     */
    private static ClassPath convertToSourcePath(File[] fs)  {
        List<PathResourceImplementation> lst = new ArrayList<PathResourceImplementation>();
        Set<URL> existingSrc = new HashSet<URL>();
        for (int i = 0; i < fs.length; i++) {
            URL url = Utils.fileToURL(fs[i]);
            try {
                FileObject[] srcfos = SourceForBinaryQuery.findSourceRoots(url).getRoots();
                for (int j = 0; j < srcfos.length; j++) {
                    if (FileUtil.isArchiveFile(srcfos[j])) {
                        srcfos [j] = FileUtil.getArchiveRoot(srcfos [j]);
                    }
                    try {
                        url = srcfos[j].getURL();
                        if  (!url.toExternalForm().endsWith("/")) {
                            url = new URL(url.toExternalForm() + "/");
                        }
                    } catch (FileStateInvalidException ex) {
                        ErrorManager.getDefault().notify
                                (ErrorManager.EXCEPTION, ex);
                        continue;
                    } catch (MalformedURLException ex) {
                        ErrorManager.getDefault().notify
                                (ErrorManager.EXCEPTION, ex);
                        continue;
                    }
                    if (url == null)  {
                        continue;
                    }
                    if (!existingSrc.contains(url)) {
                        lst.add(ClassPathSupport.createResource(url));
                        existingSrc.add(url);
                    }
                } // for
            } catch (IllegalArgumentException ex) {
                //TODO??
            }
        }
        return ClassPathSupport.createClassPath(lst);
    }



    static URL fileToURL(File file) {
        try {
            URL url;
            url = file.toURI().toURL();
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            if  (!url.toExternalForm().endsWith("/")) { //NOI18N
                url = new URL(url.toExternalForm() + "/"); //NOI18N
            }
            return url;
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            return null;
        }
    }

}
