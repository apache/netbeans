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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Representation of jarfile with tests 
 */
public final class TestEntry {
    
    /** Hardcoded location of testdistribution relatively to nb source root. */
    private static final String TEST_DIST_DIR = "nbbuild/build/testdist"; // NOI18N;
    private final String codeNameBase;
    private final String testType;
    private final String cluster;
    private final File jarFile;
    
    /**
     * Creates a new instance of TestEntry
     */
    private TestEntry(File jarFile, String codeNameBase, String testType, String cluster) {
        this.jarFile = jarFile;
        this.codeNameBase = codeNameBase;
        this.testType = testType;
        this.cluster = cluster;
        
    }
    /**
     * get TestEntry for jarfile with tests
     * 
     * @param jarFile input file with tests
     * @return null when the file is not jarfile with tests
     */
    public static TestEntry get(File jarFile) {
        if (jarFile.getName().equals("tests.jar")) { // NOI18N
            // testtype/cluster/codenamebase/tests.jar
            String tokens[] = jarFile.getPath().split("[/\\\\]"); // NOI18N
            int len = tokens.length;
            if (len > 3 ) {
               String cnb = tokens[len - 2].replace('-','.') ;
               String cluster = tokens[len - 3];
               String testType = tokens[len - 4];
               return new TestEntry(jarFile, cnb, testType, cluster);
            }
        }
        return null;
    }  
    
    public String getCodeNameBase() {
        return codeNameBase;
    }

    public String getCluster() {
        // set default cluster for modules in module suite
        return (cluster == null) ? "cluster" : cluster; // NOI18N
    }

    public File getJarFile() {
        return jarFile;
    }
    
    /** Get root folder of binary tests distribution.
     */
    public File getTestDistRoot() {
        File ret = getJarFile();
        for (int i = 0; i < 4 && ret != null; i++) {
            ret = ret.getParentFile();
        }
        return ret;
    }
    
    /** Get source dir with tests.
     *  @return null if source dir was not located 
     */
    public URL getSrcDir() throws IOException {
        String nborgPath = getNetBeansOrgPath();
        if (nborgPath != null) {
            return Utilities.toURI(new File(getNBRoot(),nborgPath)).toURL();
        }
        File prjDir = getTestDistRoot();
        if (prjDir == null)
            return null;
        // find parent when dir was not created
        while(!prjDir.exists()) {
            prjDir = prjDir.getParentFile();
            if (prjDir == null) {
                // parent doesn't exist
                return null;
            } 
        }
        Project prj = FileOwnerQuery.getOwner(FileUtil.toFileObject(prjDir));
        if (prj != null && prj.getLookup().lookup(SuiteProject.class) != null) {
            SubprojectProvider subprojects = prj.getLookup().lookup(SubprojectProvider.class);
            if (subprojects != null) {
                for (Project p : subprojects.getSubprojects()) {
                    if (p instanceof NbModuleProject) {
                        NbModuleProject nbm = (NbModuleProject) p;
                        if (nbm != null && nbm.getCodeNameBase().equals(getCodeNameBase())) {
                            FileObject file = nbm.getTestSourceDirectory(testType);
                            if (file != null) {
                                return file.toURL();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    File getNBRoot() {
        File rootDir = getTestDistRoot();
        if (rootDir == null)
            return null;
        String path = rootDir.getAbsolutePath().replace(File.separatorChar,'/');
        File nbroot = null;
        // hardcoded location of testdistribution relatively to nb source root
        if (path.endsWith(TEST_DIST_DIR)) { 
            nbroot = rootDir.getParentFile().getParentFile().getParentFile();
        }
        return nbroot;
    } 
    
    public String getNetBeansOrgPath () throws IOException {
        File nbroot =  getNBRoot();
        if (nbroot != null && ModuleList.isNetBeansOrg(nbroot) ) {
            ModuleList list = ModuleList.findOrCreateModuleListFromNetBeansOrgSources(nbroot);
            ModuleEntry entry = list.getEntry(codeNameBase);
            if (entry == null) {
                return null;
            }
            return entry.getNetBeansOrgPath() + "/test/" + getTestType() + "/src";
        }
        return null;
    }

    public String getTestType() {
        return testType;
    }

    /** 
     *  Get project for TestEntry
     *  @return null when project was not found
     */
    public Project getProject() {
        try {
            URL  url = getSrcDir();
            if (url != null) {
                URI uri = url.toURI();
                if (uri != null) {
                    return FileOwnerQuery.getOwner(uri);
                }
            }
        } catch (IOException ex) {
            Util.err.notify(ex);
        } catch (URISyntaxException ex) {
            Util.err.notify(ex);
        }
        return null;
    }

    @Override
    public String toString() {
        try {
            return "TestEntry[" + jarFile + ",src=" + getSrcDir() + "]"; // NOI18N
        } catch (IOException x) {
            return "TestEntry[" + jarFile + ",src=<" + x.getMessage() + ">]"; // NOI18N
        }
    }
    
} 
