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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.universe.TestEntry;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Provides sources for module build products.
 */
public final class SourceForBinaryImpl implements SourceForBinaryQueryImplementation {
    
    private final NbModuleProject project;
    private URL classesUrl;
    private final Map<String,URL> testClassesUrl = new HashMap<String,URL>();
    private Map<URL,SourceForBinaryQuery.Result> cache = new HashMap<URL,SourceForBinaryQuery.Result>();

    public SourceForBinaryImpl(NbModuleProject project) {
        this.project = project;
    }
    
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        //System.err.println("findSourceRoot: " + binaryRoot);
        SourceForBinaryQuery.Result res = cache.get(binaryRoot);
        if (res == null) {
            File binaryJarF = FileUtil.archiveOrDirForURL(binaryRoot);
            if (binaryJarF != null) {
                FileObject srcDir = null;
                File genSrcDirF = null;
                if (binaryJarF.getAbsolutePath().endsWith(project.evaluator().getProperty("module.jar").replace('/', File.separatorChar))) {
                    srcDir = project.getSourceDirectory();
                    genSrcDirF = project.getGeneratedClassesDirectory();
                } else {
                    // maybe tests.jar in testdistribution
                    TestEntry entry = TestEntry.get(binaryJarF);
                    if (entry != null && project.getCodeNameBase().equals(entry.getCodeNameBase())) {
                        String type = entry.getTestType();
                        srcDir = project.getTestSourceDirectory(type);
                        genSrcDirF = project.getTestGeneratedClassesDirectory(type);
                    } else {
                        // try classpath-extension
                        // // convention-over-cfg per mkleint's suggestion: <jarname>-src(.zip) folder or ZIP first
                        // TODO - cache all results?!? also for JFBQI
                        String n = binaryJarF.getName();
                        if (n.endsWith(".jar")) { // NOI18N
                            File jFolder = new File(binaryJarF.getParentFile(),
                                    n.substring(0, n.length() - ".jar".length()) + "-src");
                            if (jFolder.isDirectory()) {
                                res = new Result(FileUtil.toFileObject(jFolder), null);
                                cache.put(binaryRoot,res);
                                return res;
                            } else {
                                File jZip = new File(jFolder.getAbsolutePath() + ".zip");
                                if (jZip.isFile()) {
                                    res = new Result(FileUtil.getArchiveRoot(FileUtil.toFileObject(jZip)), null);
                                    cache.put(binaryRoot,res);
                                    return res;
                                }
                            }
                        }
                    }
                }
                if (srcDir != null) {
                    res = new Result(srcDir, genSrcDirF);
                    cache.put(binaryRoot,res);
                    return res;
                }
            }
            if (binaryRoot.equals(getClassesUrl())) {
                FileObject srcDir = project.getSourceDirectory();
                if (srcDir != null) {
                    res = new Result(srcDir, project.getGeneratedClassesDirectory());
                }
            } else {
                for (String testType : project.supportedTestTypes()) {
                    if (binaryRoot.equals(getTestClassesUrl(testType))) {
                        FileObject testSrcDir = project.getTestSourceDirectory(testType);
                        if (testSrcDir != null) {
                            res = new Result(testSrcDir, project.getTestGeneratedClassesDirectory(testType));
                            break;
                        }
                    }
                }
                if (res == null) {
                // Check extra compilation units.
                ECUS: for (Map.Entry<FileObject,Element> entry : project.getExtraCompilationUnits().entrySet()) {
                    for (Element kid : XMLUtil.findSubElements(entry.getValue())) {
                        if (!kid.getLocalName().equals("built-to")) { // NOI18N
                            continue;
                        }
                        String rawtext = XMLUtil.findText(kid);
                        assert rawtext != null : "Null content for <built-to> in " + project;
                        String text = project.evaluator().evaluate(rawtext);
                        if (text == null) {
                            continue;
                        }
                        File loc = project.getHelper().resolveFile(text);
                        URL u = FileUtil.urlForArchiveOrDir(loc);
                        if (u == null) { // #135163
                            Logger.getLogger(SourceForBinaryImpl.class.getName()).log(Level.WARNING,
                                    "In " + FileUtil.getFileDisplayName(project.getProjectDirectory()) +
                                    " " + loc + " is neither a directory nor a JAR");
                        } else if (u.equals(binaryRoot)) {
                            res = new Result(entry.getKey(), null);
                            break ECUS;
                        }
                    }
                }
                }
            }
            if (res != null) {
                cache.put(binaryRoot,res);
            }
        }
        return res;
    }
    
    private URL getClassesUrl() {
        if (classesUrl == null) {
            classesUrl = FileUtil.urlForArchiveOrDir(project.getClassesDirectory());
        }
        return classesUrl;
    }
    
    private URL getTestClassesUrl(String testType) {
        if (!testClassesUrl.containsKey(testType) && project.supportedTestTypes().contains(testType)) {
            File testClassesDir = project.getTestClassesDirectory(testType);
            testClassesUrl.put(testType, FileUtil.urlForArchiveOrDir(testClassesDir));
        }
        return testClassesUrl.get(testType);
    }
    
    
    private static class Result implements SourceForBinaryQuery.Result {
               
        private FileObject[] res;
        
        public Result(FileObject res, File other) {
            assert res != null;
            FileObject otherFO = other != null ? FileUtil.toFileObject(other) : null;
            if (otherFO != null) {
                this.res = new FileObject[] {res, otherFO};
            } else {
                this.res = new FileObject[] {res};
            }
        }
        
        public FileObject[] getRoots () {
            return res;
        }
        
        public void addChangeListener (ChangeListener l) {
            //Not needed, do not suppose the source root to be changed in nbproject
        }
        
        public void removeChangeListener (ChangeListener l) {
            //Not needed, do not suppose the source root to be changed in nbproject
        }
        
    }
    
}
